package cn.micro.lemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Lemon Status Code
 *
 * @author lry
 */
@Slf4j
@Getter
@ToString
@AllArgsConstructor
public enum LemonStatusCode {

    // ======= Lemon Framework Exception

    SUCCESS(200, "Success"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Token Expired"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    CALL_ORIGINAL_TIMEOUT(601, "Call Original Timeout"),
    CALL_ORIGINAL_BIZ_ERROR(602, "Call Original BIZ Error"),
    CALL_ORIGINAL_NETWORK_ERROR(603, "Call Original Network Error"),
    CALL_ORIGINAL_SERIALIZATION(604, "Call Original Serialization"),
    CALL_ORIGINAL_UNKNOWN(699, "Call Original Unknown");

    private final int code;
    private final String message;

}
