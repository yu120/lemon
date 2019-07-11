package cn.micro.lemon.dubbo;

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
     * The registry address
     */
    private String registryAddress;
    /**
     * The metadata address
     */
    private String metadataAddress;

}
