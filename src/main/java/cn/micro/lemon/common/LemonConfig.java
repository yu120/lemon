package cn.micro.lemon.common;

import cn.micro.lemon.common.config.BizTaskConfig;
import cn.micro.lemon.common.config.JwtConfig;
import cn.micro.lemon.common.config.DubboConfig;
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

    /**
     * The operation token
     */
    private String token = "lemon";
    /**
     * The lemon http application port
     */
    private int port = 8080;
    /**
     * The lemon http application path
     */
    private String application;
    /**
     * The registry address
     */
    private String lemonRegistry;

    /**
     * The IO thread number
     */
    private int ioThread = 0;
    /**
     * The work thread number
     */
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

    private BizTaskConfig biz;

    /**
     * The configure fixed response header list
     */
    private Map<String, Object> resHeaders = new LinkedHashMap<>();

    /**
     * The global call original server timeout(ms)
     */
    private long originalTimeout = 30000L;
    /**
     * The call original request header list key
     */
    private Set<String> originalReqHeaders = new LinkedHashSet<>();
    /**
     * The call original response header list key
     */
    private Set<String> originalResHeaders = new LinkedHashSet<>();

    /**
     * The exclude filter list
     */
    private List<String> excludeFilters = new ArrayList<>();
    /**
     * The include filter list
     */
    private List<String> includeFilters = new ArrayList<>();

    /**
     * The jwt config
     */
    private JwtConfig jwt;
    /**
     * The dubbo config
     */
    private DubboConfig dubbo;
    /**
     * The direct connection service mapping list
     */
    private List<ServiceMapping> services = new ArrayList<>();

}
