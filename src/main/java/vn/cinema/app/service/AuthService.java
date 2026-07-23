package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.LoginRequest;
import vn.cinema.app.dto.request.RegisterRequest;
import vn.cinema.app.dto.response.LoginResponse;
import vn.cinema.config.JwtProperties;
import vn.cinema.domain.common.exception.*;
import vn.cinema.domain.user.entity.RefreshToken;
import vn.cinema.domain.user.repository.RefreshTokenRepository;
import vn.cinema.domain.user.repository.UserRepository;
import vn.cinema.domain.user.entity.User;
import vn.cinema.domain.user.entity.UserRole;
import vn.cinema.domain.user.entity.UserStatus;
import vn.cinema.infrastructure.security.JwtTokenService;
import vn.cinema.infrastructure.utility.RefreshTokenGenerator;
import vn.cinema.infrastructure.utility.RefreshTokenHasher;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final RefreshTokenGenerator refreshTokenGenerator;

    /**
     * Đăng nhập hệ thống bằng Email và Mật khẩu.
     * <p>
     * - Xác thực thông tin qua Spring Security AuthenticationManager.<br>
     * - Sinh Access Token (JWT) thời hạn ngắn.<br>
     * - Sinh Refresh Token thô (raw), mã hóa SHA-256 rồi lưu vào DB với thời hạn 7 ngày.
     * </p>
     *
     * @param request DTO chứa thông tin email và password từ client
     * @return {@link LoginResponse} chứa Access Token, Refresh Token (raw), TokenType và thời gian hết hạn
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 1. Xác thực email + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        // 2. Tạo access token
        String accessToken = jwtTokenService.generateAccessToken(authentication);

        // 3. Tạo refresh token + lưu DB
        String rawRefreshToken = refreshTokenGenerator.generate();

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        BusinessErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(RefreshTokenHasher.hash(rawRefreshToken))
                .expiresAt(clock.instant().plus(7, ChronoUnit.DAYS))
                .revoked((short) 0)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtProperties.expirationMs / 1000
        );
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     * <p>
     * - Chuẩn hóa email (cắt khoảng trắng, chuyển thành chữ thường).<br>
     * - Kiểm tra trùng lặp email ở mức ứng dụng và bắt lỗi Unique Index ở mức DB (chống Race Condition).<br>
     * - Mã hóa mật khẩu bằng BCrypt trước khi lưu.
     * </p>
     *
     * @param request DTO chứa thông tin email và password đăng ký
     */
    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // The database has a case-insensitive unique index, which also protects
            // against two concurrent registrations using the same email.
            throw new ConflictException("Email is already registered");
        }
    }

    /**
     * Cấp Access Token mới khi Access Token cũ đã hết hạn.
     * <p>
     * - Băm SHA-256 chuỗi refreshToken thô từ client truyền lên để tìm kiếm trong DB.<br>
     * - Kiểm tra tính hợp lệ của token (chưa bị thu hồi/revoked và chưa hết hạn/expired).<br>
     * - Cấp lại Access Token mới mà không cần cấp lại Refresh Token.
     * </p>
     *
     * @param refreshToken Chuỗi Refresh Token dạng thô (raw string) nhận từ client
     * @return {@link LoginResponse} chứa Access Token mới
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        Instant now = clock.instant();
        String tokenHash = RefreshTokenHasher.hash(refreshToken);

        // Tìm refresh token: để check xem có tồn tại hoặc bị revoke
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException(BusinessErrorCode.RESOURCE_NOT_FOUND, "Refresh token not found"));

        if (currentToken.isRevoked()) {
            throw new RefreshTokenRevokedException(BusinessErrorCode.REFRESH_TOKEN_REVOKED, "Refresh token has been revoked");
        }

        if (currentToken.isExpired(now)) {
            throw new RefreshTokenExpiredException(BusinessErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token has expired");
        }

        // Tạo access token mới
        User user = currentToken.getUser();
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user.getEmail(),
                null,
                authorities
        );

        String accessToken = jwtTokenService.generateAccessToken(authentication);

        return new LoginResponse(
                accessToken,
                null,
                "Bearer",
                jwtProperties.expirationMs / 1000
        );
    }

    /**
     * Đăng xuất khỏi thiết bị hiện tại (Vô hiệu hóa 1 Refresh Token).
     * <p>
     * - Băm SHA-256 chuỗi refreshToken thô từ client để tìm record tương ứng trong DB.<br>
     * - Đánh dấu `revoked = 1` để ngăn token này tiếp tục xin cấp Access Token mới.
     * </p>
     *
     * @param refreshToken Chuỗi Refresh Token dạng thô (raw string) cần vô hiệu hóa
     */
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = RefreshTokenHasher.hash(refreshToken);
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException(BusinessErrorCode.RESOURCE_NOT_FOUND, "Refresh token not found"));
        refreshTokenEntity.revoke();
    }

    /**
     * Đăng xuất khỏi tất cả các thiết bị (Vô hiệu hóa toàn bộ Refresh Token của User).
     * <p>
     * - Tìm kiếm người dùng dựa trên email (lấy từ Access Token của người dùng đang đăng nhập).<br>
     * - Thực thi 1 câu lệnh Bulk UPDATE trực tiếp ở DB để chuyển `revoked = 1` cho tất cả token còn active của user.
     * </p>
     *
     * @param email Email của người dùng hiện tại (được trích xuất tự động từ Access Token)
     */
    @Transactional
    public void logoutAll(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        BusinessErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        refreshTokenRepository.revokeAllActiveByUserId(user.getId());
    }

}
