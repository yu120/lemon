package org.micro.lemon.filter.support;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.config.JwtConfig;
import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.server.LemonContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;

import java.io.UnsupportedEncodingException;

/**
 * LemonJwtFilter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "jwt", order = 20)
public class LemonJwtFilter extends AbstractFilter {

    private JwtConfig jwtConfig;
    private Algorithm algorithm;
    private JWTVerifier verifier;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.jwtConfig = lemonConfig.getJwt();
        if (!jwtConfig.isEnable()) {
            return;
        }

        try {
            switch (jwtConfig.getAlgorithm()) {
                case HMAC256:
                    this.algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
                    break;
                case HMAC384:
                    this.algorithm = Algorithm.HMAC384(jwtConfig.getSecret());
                    break;
                case HMAC512:
                    this.algorithm = Algorithm.HMAC512(jwtConfig.getSecret());
                    break;
                default:
            }
            this.verifier = JWT.require(algorithm).build();
        } catch (UnsupportedEncodingException e) {
            log.error("LemonJwtFilter initialize exception", e);
        }
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        if (jwtConfig.isEnable()) {
            Object token = context.getHeaderValue(jwtConfig.getKey());
            if (token == null) {
                context.onCallback(LemonStatusCode.BAD_REQUEST, "'" + jwtConfig.getKey() + "' is null or empty");
                return;
            }

            try {
                verifier.verify(String.valueOf(token));
            } catch (TokenExpiredException e) {
                context.onCallback(LemonStatusCode.PAYMENT_REQUIRED, "Token expired");
                return;
            } catch (JWTVerificationException e) {
                context.onCallback(LemonStatusCode.UNAUTHORIZED, "Verify that the token is illegal");
                return;
            }
        }

        super.preFilter(chain, context);
    }

}
