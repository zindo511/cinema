package vn.cinema.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingCodeGenerator {

    private static final String PREFIX = "BK";
    private static final int RANDOM_LENGTH = 16;

    public String generate() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, RANDOM_LENGTH)
                .toUpperCase();

        return PREFIX + randomPart;
    }
}
