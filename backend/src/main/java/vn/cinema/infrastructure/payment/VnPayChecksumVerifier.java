package vn.cinema.infrastructure.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.cinema.config.VnPayProperties;
import vn.cinema.infrastructure.utility.VnPayUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class VnPayChecksumVerifier {

    private static final Set<String> EXCLUDED_FIELDS = Set.of(
            "vnp_SecureHash",
            "vnp_SecureHashType"
    );

    private final VnPayProperties vnPayProperties;

    public boolean verify(Map<String, String> requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            return false;
        }

        String receivedHash = requestParams.get("vnp_SecureHash");

        // HMAC-SHA512 biểu diễn dưới dạng hex gồm 128 ký tự
        if (receivedHash == null ||
                !receivedHash.matches("(?i)^[0-9a-f]{128}$")) {
            return false;
        }

        Map<String, String> signedParams = new HashMap<>();

        requestParams.forEach((key, value) -> {
            if (key.startsWith("vnp_")
                    && !EXCLUDED_FIELDS.contains(key)
                    && value != null
                    && !value.isEmpty()) {
                signedParams.put(key, value);
            }
        });

        String signData = VnPayUtil.buildQueryUrl(signedParams);

        String expectedHash = VnPayUtil.hmacSHA512(
                vnPayProperties.getHashSecret(),
                signData
        );

        return constantTimeEquals(expectedHash, receivedHash);
    }

    private boolean constantTimeEquals(String expected, String received) {
        byte[] expectedBytes = expected
                .toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);

        byte[] receivedBytes = received
                .toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);

        return MessageDigest.isEqual(expectedBytes, receivedBytes);
    }
}
