package org.micro.lemon.common.config;

import org.micro.lemon.common.support.JwtAlgorithm;
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
     * The jwt enable
     */
    private boolean enable = true;
    /**
     * The jwt key
     */
    private String key = "Token";
    /**
     * The jwt secret
     */
    private String secret = "lemon";
    /**
     * The jwt algorithm
     */
    private JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;

}
