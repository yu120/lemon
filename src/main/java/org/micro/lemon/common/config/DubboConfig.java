package org.micro.lemon.common.config;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Dubbo Config
 *
 * @author lry
 */
@Data
@ToString
public class DubboConfig implements Serializable {

    /**
     * The service name simple
     */
    private Boolean serviceSimpleName = true;
    /**
     * The registry address
     */
    private String registryAddress;
    /**
     * The metadata address
     */
    private String metadataAddress;

}
