package cn.micro.lemon.common.support;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Jwt Config
 *
 * @author lry
 */
@Data
@ToString
public class JwtConfig implements Serializable {

    /**
     * The jwt secret
     */
    private String secret = "lemon";
    /**
     * The jwt key address
     */
    private KeyAddr keyAddr = KeyAddr.HEADER;
    /**
     * The jwt key
     */
    private String key = "Token";
    /**
     * The jwt algorithm
     */
    private JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;

    /**
     * Jwt Key Addr
     *
     * @author lry
     */
    public enum KeyAddr {
        // ===
        HEADER, QUERY;
    }

    public enum JwtAlgorithm {
        // ===
        HMAC256, HMAC384, HMAC512;
    }

}
