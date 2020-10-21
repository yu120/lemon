package org.micro.lemon.proxy.dubbo.metadata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LemonService
 *
 * @author lry
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LemonService {

    /**
     * The service api path
     *
     * @return return api path
     */
    String value() default "";

    /**
     * The service api name
     *
     * @return name document
     */
    String name() default "";

    /**
     * The true indicates that access requires authentication
     *
     * @return true is authentication
     */
    boolean auth() default false;

    /**
     * The service api description
     *
     * @return description document
     */
    String msg() default "";

}
