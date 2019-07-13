package cn.micro.lemon;

import cn.micro.lemon.dubbo.DubboConfig;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

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

    /**
     * The default value: 64MB
     */
    private int maxContentLength = 1024 * 1024 * 64;
    /**
     * The max server conn (all clients conn)
     **/
    private int maxChannel = 100000;
    /**
     * The server model: true
     */
    private boolean server = true;

    private int bizCoreThread = 20;
    private int bizMaxThread = 200;
    private int bizQueueCapacity = 800;
    private long bizKeepAliveTime = 60000L;

    private Map<String, Object> resHeaders = new LinkedHashMap<>();
    private Set<String> originalHeaders = new LinkedHashSet<>();

    private boolean wrapperMeta = true;
    private DubboConfig dubbo;
    private List<ServiceMapping> services = new ArrayList<>();

}
