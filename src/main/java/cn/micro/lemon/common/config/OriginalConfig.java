package cn.micro.lemon.common.config;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Jwt Config
 *
 * @author lry
 */
@Data
@ToString
public class OriginalConfig implements Serializable {

    /**
     * The global call original server timeout(ms)
     */
    private long timeout = 30000L;
    /**
     * The call original request header list key
     */
    private Set<String> reqHeaders = new LinkedHashSet<>();
    /**
     * The call original response header list key
     */
    private Set<String> resHeaders = new LinkedHashSet<>();

}
