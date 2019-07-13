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
    NOT_LOGGED_ON(402, "Not Logged On"),
    TOKEN_EXPIRED(403, "Token Has Expired"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    CALL_ORIGINAL_TIMEOUT(601, "Call Original Timeout"),
    CALL_ORIGINAL_BIZ_ERROR(602, "Call Original BIZ Error"),
    CALL_ORIGINAL_NETWORK_ERROR(603, "Call Original Network Error"),
    CALL_ORIGINAL_SERIALIZATION(604, "Call Original Serialization"),
    CALL_ORIGINAL_UNKNOWN(699, "Call Original Unknown");

    private final int code;
    private final String message;

}
