package vn.cinema.infrastructure.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

/**
 * Custom handler for 403 Forbidden responses when a user is authenticated
 * but lacks the required role/authority to access a resource.
 *
 * This ensures the error format matches the GlobalExceptionHandler structure.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String json = """
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "Forbidden",
                  "code": "ACCESS_DENIED",
                  "message": "You do not have permission to access this resource"
                }
                """.formatted(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value()
        );

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }
}
