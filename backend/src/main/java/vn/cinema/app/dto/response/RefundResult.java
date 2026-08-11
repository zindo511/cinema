package vn.cinema.app.dto.response;

public record RefundResult(
        String providerResponseId,
        String providerTransactionNo,
        String responseCode,
        String transactionStatus,
        String message
) {
}
