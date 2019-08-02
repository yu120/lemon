package cn.micro.lemon.filter.support;

import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.common.LemonStatusCode;
import cn.micro.lemon.common.config.JwtConfig;
import cn.micro.lemon.common.support.JwtKeyAddr;
import cn.micro.lemon.filter.AbstractFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.server.LemonContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

import java.io.UnsupportedEncodingException;

/**
 * Lemon Jwt Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "jwt", order = 30)
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

        String token;
        if (JwtKeyAddr.QUERY == jwtConfig.getJwtKeyAddr()) {
            token = context.getHeaders().get(jwtConfig.getKey());
        } else {
            token = context.getParameters().get(jwtConfig.getKey());
        }

        if (token == null || token.length() == 0) {
            context.writeAndFlush(LemonStatusCode.NOT_FOUND, "'" + jwtConfig.getKey() + "' is Null or Empty");
            return;
        }

        try {
            verifier.verify(token);
            super.preFilter(chain, context);
        } catch (AlgorithmMismatchException e) {
            context.writeAndFlush(LemonStatusCode.BAD_REQUEST, "Algorithm Mismatch");
        } catch (InvalidClaimException e) {
            context.writeAndFlush(LemonStatusCode.BAD_REQUEST, "Invalid Claim");
        } catch (JWTDecodeException e) {
            context.writeAndFlush(LemonStatusCode.BAD_REQUEST, "JWT Decode");
        } catch (SignatureVerificationException e) {
            context.writeAndFlush(LemonStatusCode.BAD_REQUEST, "Signature Verification");
        } catch (TokenExpiredException e) {
            context.writeAndFlush(LemonStatusCode.PAYMENT_REQUIRED, "Token Expired");
        } catch (Exception e) {
            context.writeAndFlush(LemonStatusCode.BAD_REQUEST, "JWT Verify Unknown Exception");
        }
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        if (!jwtConfig.isEnable()) {
            super.postFilter(chain, context);
            return;
        }

        super.postFilter(chain, context);
    }

}
