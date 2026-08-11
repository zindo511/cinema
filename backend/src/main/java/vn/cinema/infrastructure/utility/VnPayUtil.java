package vn.cinema.infrastructure.utility;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class VnPayUtil {

    private VnPayUtil() {}

    /**
     * Xây dựng query string từ map các tham số VNPay.
     *
     * <p>Chuỗi query được xây dựng theo yêu cầu của VNPay:</p>
     * <ul>
     *   <li>Các tham số được sắp xếp theo thứ tự alphabet của tên (key).</li>
     *   <li>Chỉ các tham số có giá trị không rỗng mới được đưa vào.</li>
     *   <li>Cả key và value đều được URL-encode theo chuẩn UTF-8,
     *       với dấu {@code +} được thay bằng {@code %20} (theo RFC 3986).</li>
     *   <li>Các cặp key=value được nối bằng dấu {@code &}.</li>
     * </ul>
     *
     * <p>Ví dụ kết quả:</p>
     * <pre>
     * vnp_Amount=1000000&vnp_Command=pay&vnp_TmnCode=DEMO1234
     * </pre>
     *
     * @param params map chứa tên và giá trị các tham số thanh toán VNPay
     * @return chuỗi query string đã được sắp xếp và URL-encode
     */
    public static String buildQueryUrl(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        // Sắp xếp các tham số theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder queryUrl = new StringBuilder();

        fieldNames.forEach((fieldName) -> {
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!queryUrl.isEmpty()) {
                    queryUrl.append("&");
                }

                // URL encode cả key và value
                queryUrl.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                queryUrl.append("=");
                queryUrl.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        });

        return queryUrl.toString();
    }

    /**
     * Tạo chữ ký HMAC-SHA512 từ dữ liệu và secret key.
     *
     * <p>VNPay sử dụng thuật toán HMAC-SHA512 để ký dữ liệu giao dịch.
     * Chuỗi hash được trả về dưới dạng hexadecimal (128 ký tự).</p>
     *
     * <p>Quy trình:</p>
     * <ol>
     *   <li>Khởi tạo {@link Mac} với thuật toán HmacSHA512.</li>
     *   <li>Nạp secret key vào Mac instance.</li>
     *   <li>Tính toán HMAC trên dữ liệu đầu vào (UTF-8).</li>
     *   <li>Chuyển kết quả byte array sang chuỗi hex lowercase.</li>
     * </ol>
     *
     * @param key  secret key (vnp_HashSecret) từ VNPay cung cấp
     * @param data chuỗi dữ liệu cần ký (thường là query string đã sắp xếp)
     * @return chuỗi hex 128 ký tự (HMAC-SHA512 digest)
     * @throws IllegalStateException nếu thuật toán HmacSHA512 không khả dụng
     *                               hoặc secret key không hợp lệ
     */

    public static String hmacSHA512(String key, String data) {
        if (key == null || data == null) {
            throw new IllegalArgumentException("key or data is null");
        }

        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(
                    StandardCharsets.UTF_8), "HmacSHA512"
            );

            hmac512.init(secretKey);

            byte[] hashBytes = hmac512.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(
                    "Failed to generate HMAC-SHA512 signature", e
            );
        }
    }
}
