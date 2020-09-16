package org.micro.lemon.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.micro.lemon.common.config.BizTaskConfig;
import org.micro.lemon.common.config.JwtConfig;
import org.micro.lemon.common.config.DubboConfig;
import org.micro.lemon.common.config.OriginalConfig;
import lombok.Data;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern LINE_PATTERN = Pattern.compile("-(\\w)");


    /**
     * The load config
     *
     * @return {@link LemonConfig}
     */
    public static LemonConfig loadConfig() {
        java.net.URL url = LemonConfig.class.getClassLoader().getResource("lemon.yml");
        if (url != null) {
            try {
                Iterable<Object> iterable = new Yaml().loadAll(new FileInputStream(url.getFile()));
                for (Object object : iterable) {
                    JSON json = recursion(object);
                    return json.toJavaObject(LemonConfig.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("The load as yaml is exception", e);
            }
        }

        throw new RuntimeException("Not found lemon.yml");
    }

    /**
     * 递归解析
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    private static JSON recursion(Object object) {
        if (object instanceof Map) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) object).entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (key.startsWith("-D") || key.startsWith("-d")) {
                    key = key.substring(2);
                } else if (key.contains("-")) {
                    key = lineToHump(key);
                }
                if (entry.getValue() instanceof Map || entry.getValue() instanceof Collection) {
                    jsonObject.put(key, recursion(entry.getValue()));
                } else {
                    jsonObject.put(key, entry.getValue());
                }
            }

            return jsonObject;
        } else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object tempObject : (List<Object>) object) {
                if (tempObject instanceof Map || tempObject instanceof Collection) {
                    jsonArray.add(recursion(tempObject));
                } else {
                    jsonArray.add(tempObject);
                }
            }

            return jsonArray;
        } else {
            throw new RuntimeException("未知数据类型" + object);
        }
    }

    /**
     * 减号转驼峰
     */
    private static String lineToHump(String str) {
        Matcher matcher = LINE_PATTERN.matcher(str.toLowerCase());
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
