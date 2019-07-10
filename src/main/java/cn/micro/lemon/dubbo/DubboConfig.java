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

    private String registryAddress;
    private String metadataAddress;

}
