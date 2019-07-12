package cn.micro.lemon;

import cn.micro.lemon.dubbo.DubboConfig;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lemon Config
 *
 * @author lry
 */
@Data
@ToString
public class LemonConfig implements Serializable {

    private String protocol;
    private int port = 8080;
    private String application;
    private int ioThread = 0;
    private int workThread = 0;

    private int bizCoreThread = 20;
    private int bizMaxThread = 200;
    private int bizQueueCapacity = 800;
    private long bizKeepAliveTime = 60000L;

    private Map<String, Object> resHeaders = new LinkedHashMap<>();

    private boolean wrapperMeta = true;
    private DubboConfig dubbo;
    private List<ServiceMapping> services;

}
