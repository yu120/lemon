package org.micro.lemon.common;

import org.micro.lemon.common.config.BizTaskConfig;
import org.micro.lemon.common.config.JwtConfig;
import org.micro.lemon.common.config.DubboConfig;
import org.micro.lemon.common.config.OriginalConfig;
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
     * The lemon http application path
     */
    private String application;
    /**
     * The lemon http application port
     */
    private int port = 8080;
    /**
     * The server model: true
     */
    private boolean server = true;
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
     * The biz task config
     */
    private BizTaskConfig biz;
    /**
     * The original config
     */
    private OriginalConfig original;
    /**
     * The jwt config
     */
    private JwtConfig jwt;
    /**
     * The dubbo config
     */
    private DubboConfig dubbo;


    /**
     * The registry address
     */
    private String registryAddress;
    /**
     * The exclude filter list
     */
    private List<String> excludeFilters = new ArrayList<>();
    /**
     * The include filter list
     */
    private List<String> includeFilters = new ArrayList<>();
    /**
     * The configure fixed response header list
     */
    private Map<String, Object> resHeaders = new LinkedHashMap<>();
    /**
     * The direct connection service mapping list
     */
    private List<ServiceMapping> services = new ArrayList<>();

}
