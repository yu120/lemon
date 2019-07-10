package cn.micro.lemon;

import cn.micro.lemon.dubbo.DubboConfig;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Micro Service Config
 *
 * @author lry
 */
@Data
@ToString
public class MicroConfig implements Serializable {

    private String protocol;
    private int port = 8080;
    private String application;
    private DubboConfig dubbo;
    private List<ServiceMapping> services;

}
