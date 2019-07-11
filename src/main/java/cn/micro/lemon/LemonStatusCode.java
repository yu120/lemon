package cn.micro.lemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Micro Status Code
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

    MICRO_BAD_REQUEST_EXCEPTION(400, "Bad Request"),
    MICRO_PERMISSION_EXCEPTION(401, "Unauthorized"),
    MICRO_TOKEN_NOT_FOUND_EXCEPTION(402, "Not Logged On"),
    MICRO_TOKEN_EXPIRED_EXCEPTION(403, "Token Has Expired"),
    NO_HANDLER_FOUND_EXCEPTION(404, "Not Found"),
    MICRO_ERROR_EXCEPTION(500, "Internal Server Error"),

    CALL_ORIGINAL_TIMEOUT(601, "Call Original Timeout"),
    CALL_ORIGINAL_BIZ_ERROR(602, "Call Original BIZ Error"),
    CALL_ORIGINAL_NETWORK_ERROR(603, "Call Original Network Error"),
    CALL_ORIGINAL_SERIALIZATION(604, "Call Original Serialization"),
    CALL_ORIGINAL_UNKNOWN(699, "Call Original Unknown");

    private final Integer code;
    private final String message;

}
