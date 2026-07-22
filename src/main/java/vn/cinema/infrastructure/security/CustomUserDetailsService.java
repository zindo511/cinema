package vn.cinema.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.cinema.domain.user.repository.UserRepository;
import vn.cinema.domain.user.entity.User;
import vn.cinema.domain.user.entity.UserStatus;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())           // enum name() → "USER", "STAFF", "ADMIN"
                .disabled(user.getStatus() == UserStatus.DELETED)
                .accountLocked(user.getStatus() == UserStatus.LOCKED)
                .build();
    }
}
