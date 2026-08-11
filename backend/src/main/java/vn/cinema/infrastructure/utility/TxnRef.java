package vn.cinema.infrastructure.utility;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TxnRef {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final DateTimeFormatter TXN_REF_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String generateTxnRef() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(TXN_REF_FORMATTER);

        String randomPart = String.format(
                Locale.ROOT,
                "%06d",
                RANDOM.nextInt(1_000_000)
        );

        return datePart + randomPart;
    }
}
