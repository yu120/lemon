package org.micro.lemon.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Service Mapping
 *
 * @author lry
 */
@Data
@ToString
public class ServiceMapping implements Serializable {

    /**
     * The category
     */
    private String category;
    /**
     * The application of service
     */
    private String application;
    /**
     * The service name
     */
    private String service;
    /**
     * The real service name
     */
    private String serviceName;
    /**
     * The group of service
     */
    private String group;
    /**
     * The version of service
     */
    private String version;
    /**
     * The url of service direct connection
     */
    private String url;
    /**
     * The full url is true
     */
    private boolean fullUrl = false;
    /**
     * The current call original server timeout(ms)
     */
    private Long timeout;

    /**
     * The method name
     */
    private String method;
    /**
     * The parameter type list
     */
    private String[] paramTypes;
    /**
     * The parameter value list
     */
    private Object[] paramValues;

}
