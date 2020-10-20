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
 * Lemon Jwt Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "jwt", order = 20)
public class LemonJwtFilter extends AbstractFilter {

    private Algorithm algorithm;
    private JWTVerifier verifier;
    private JwtConfig jwtConfig;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.jwtConfig = lemonConfig.getJwt();
        if (!jwtConfig.isEnable()) {
            return;
        }

        super.initialize(lemonConfig);

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
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        if (!jwtConfig.isEnable()) {
            super.preFilter(chain, context);
            return;
        }

        Object token = context.getHeaders().get(jwtConfig.getKey());
        if (token == null) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "'" + jwtConfig.getKey() + "' is Null or Empty");
            return;
        }

        try {
            verifier.verify(String.valueOf(token));
            super.preFilter(chain, context);
        } catch (AlgorithmMismatchException e) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "Algorithm Mismatch");
        } catch (InvalidClaimException e) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "Invalid Claim");
        } catch (JWTDecodeException e) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "JWT Decode");
        } catch (SignatureVerificationException e) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "Signature Verification");
        } catch (TokenExpiredException e) {
            context.onCallback(LemonStatusCode.PAYMENT_REQUIRED, "Token Expired");
        } catch (Exception e) {
            context.onCallback(LemonStatusCode.BAD_REQUEST, "JWT Verify Unknown Exception");
        }
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        if (!jwtConfig.isEnable()) {
            return;
        }

        super.postFilter(chain, context);
    }

}
