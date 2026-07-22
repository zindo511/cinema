package vn.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProperties {

    @Value("${jwt.secret}")
    public String secretKey;

    @Value("${jwt.expiration-ms}")
    public Long expirationMs;
}
