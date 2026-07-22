package vn.cinema.infrastructure.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

/**
 * Custom entry point to return a consistent JSON error response
 * when JWT authentication fails (expired, malformed, missing, etc.).
 *
 * This ensures the error format matches the GlobalExceptionHandler structure.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String code;
        String message;

        if (authException instanceof InvalidBearerTokenException) {
            code = "TOKEN_INVALID";
            // The cause message often contains specifics like "Jwt expired at ..."
            message = authException.getMessage() != null
                    ? authException.getMessage()
                    : "The access token is invalid or has expired";
        } else {
            code = "AUTHENTICATION_REQUIRED";
            message = "Authentication is required to access this resource";
        }

        String json = """
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "Unauthorized",
                  "code": "%s",
                  "message": "%s"
                }
                """.formatted(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                code,
                escapeJson(message)
        );

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
