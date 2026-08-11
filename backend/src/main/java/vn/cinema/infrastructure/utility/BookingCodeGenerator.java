package vn.cinema.infrastructure.utility;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BookingCodeGenerator {

    private static final String PREFIX = "BK";
    // Bộ ký tự 32 phần tử, đã loại bỏ 0, O, 1, I, L để tránh nhầm lẫn khi đọc/gõ mã vé
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 6;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private final SecureRandom random = new SecureRandom();

    /**
     * Sinh mã booking có dấu gạch ngang dạng: BK260725-X8K9M3 (Độ dài: 15 ký tự)
     */
    public String generateWithHyphen() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        StringBuilder randomPart = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            randomPart.append(ALPHABET.charAt(randomIndex));
        }
        return PREFIX + datePart + "-" + randomPart;
    }
}
