package cn.micro.lemon.common.config;

import cn.micro.lemon.common.support.JwtAlgorithm;
import cn.micro.lemon.common.support.KeyAddr;
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
     * The jwt key
     */
    private String key = "Token";
    /**
     * The jwt secret
     */
    private String secret = "lemon";
    /**
     * The jwt key address
     */
    private KeyAddr keyAddr = KeyAddr.HEADER;
    /**
     * The jwt algorithm
     */
    private JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;

}
