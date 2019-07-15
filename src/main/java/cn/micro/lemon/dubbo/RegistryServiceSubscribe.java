package cn.micro.lemon.dubbo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.registry.NotifyListener;

import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.RegistryService;
import org.apache.dubbo.remoting.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registry Service Subscribe
 *
 * @author lry
 */
@Slf4j
@Getter
public class RegistryServiceSubscribe implements NotifyListener {

    private static final AtomicLong ID = new AtomicLong();
    private static final URL SUBSCRIBE = new URL(
            org.apache.dubbo.registry.Constants.ADMIN_PROTOCOL,
            NetUtils.getLocalHost(), 0, "",
            CommonConstants.INTERFACE_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.GROUP_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.VERSION_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE,
            RegistryConstants.CATEGORY_KEY, RegistryConstants.PROVIDERS_CATEGORY + ","
            + RegistryConstants.CONSUMERS_CATEGORY + ","
            + RegistryConstants.ROUTERS_CATEGORY + ","
            + RegistryConstants.CONFIGURATORS_CATEGORY,
            CommonConstants.ENABLED_KEY, CommonConstants.ANY_VALUE,
            Constants.CHECK_KEY, String.valueOf(false));

    private RegistryService registryService;
    private final ConcurrentHashMap<String, Long> URL_IDS_MAPPER = new ConcurrentHashMap<>();
    /**
     * ConcurrentMap<category, ConcurrentMap<serviceName, Map<Long, URL>>>
     */
    private final ConcurrentMap<String, ConcurrentMap<String, Map<Long, URL>>> registryCache = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("lemon-registry-subscribe").build());

    public void initialize(String registryUrl) {
        URL url = URL.valueOf(registryUrl);
        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(
                RegistryFactory.class).getExtension(url.getProtocol());
        this.registryService = registryFactory.getRegistry(url);
        threadPoolExecutor.submit(() -> {
            log.info("Init Lemon Dubbo Sync Cache...");
            registryService.subscribe(SUBSCRIBE, RegistryServiceSubscribe.this);
        });
    }

    public void update(URL oldUrl, URL newUrl) {
        registryService.unregister(oldUrl);
        registryService.register(newUrl);
    }

    public void unregister(URL url) {
        URL_IDS_MAPPER.remove(url.toFullString());
        registryService.unregister(url);
    }

    public void register(URL url) {
        registryService.register(url);
    }

    public void destroy() throws Exception {
        registryService.unsubscribe(SUBSCRIBE, this);

    }

    /**
     * 收到的通知对于 ，同一种类型数据（override、subscribe、route、其它是provider），同一个服务的数据是全量的
     */
    @Override
    public void notify(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        final Map<String, Map<String, Map<Long, URL>>> categories = new HashMap<>();
        for (URL url : urls) {
            String category = url.getParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.PROVIDERS_CATEGORY);
            // 注意：empty协议的group和version为*
            if (RegistryConstants.EMPTY_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
                ConcurrentMap<String, Map<Long, URL>> services = registryCache.get(category);
                if (services != null) {
                    String group = url.getParameter(CommonConstants.GROUP_KEY);
                    String version = url.getParameter(CommonConstants.VERSION_KEY);
                    // 注意：empty协议的group和version为*
                    if (!CommonConstants.ANY_VALUE.equals(group) && !CommonConstants.ANY_VALUE.equals(version)) {
                        services.remove(url.getServiceKey());
                    } else {
                        for (Map.Entry<String, Map<Long, URL>> serviceEntry : services.entrySet()) {
                            String service = serviceEntry.getKey();
                            if (getInterface(service).equals(url.getServiceInterface()) &&
                                    (CommonConstants.ANY_VALUE.equals(group) || StringUtils.isEquals(group, getGroup(service))) &&
                                    (CommonConstants.ANY_VALUE.equals(version) || StringUtils.isEquals(version, getVersion(service)))) {
                                services.remove(service);
                            }
                        }
                    }
                }
            } else {
                Map<String, Map<Long, URL>> services = categories.computeIfAbsent(category, k -> new HashMap<>());
                String service = generateServiceKey(url);
                Map<Long, URL> ids = services.computeIfAbsent(service, k -> new HashMap<>());
                //保证ID对于同一个URL的不可变
                if (URL_IDS_MAPPER.containsKey(url.toFullString())) {
                    ids.put(URL_IDS_MAPPER.get(url.toFullString()), url);
                } else {
                    long currentId = ID.incrementAndGet();
                    ids.put(currentId, url);
                    URL_IDS_MAPPER.putIfAbsent(url.toFullString(), currentId);
                }
            }
        }

        for (Map.Entry<String, Map<String, Map<Long, URL>>> categoryEntry : categories.entrySet()) {
            String category = categoryEntry.getKey();
            ConcurrentMap<String, Map<Long, URL>> services = registryCache.get(category);
            if (services == null) {
                services = new ConcurrentHashMap<>();
                registryCache.put(category, services);
            }
            services.putAll(categoryEntry.getValue());
        }
    }

    private String generateServiceKey(URL url) {
        String inf = url.getServiceInterface();
        if (inf == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String group = url.getParameter(CommonConstants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = url.getParameter(CommonConstants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }

        return buf.toString();
    }

    private String getInterface(String serviceKey) {
        if (StringUtils.isEmpty(serviceKey)) {
            throw new IllegalArgumentException("serviceKey must not be null");
        }

        // serviceKey serviceKey=group/interface:version
        int groupIndex = serviceKey.indexOf("/");
        int versionIndex = serviceKey.indexOf(":");
        if (groupIndex > 0 && versionIndex > 0) {
            return serviceKey.substring(groupIndex + 1, versionIndex);
        } else if (groupIndex > 0 && versionIndex < 0) {
            return serviceKey.substring(groupIndex + 1);
        } else if (groupIndex < 0 && versionIndex > 0) {
            return serviceKey.substring(0, versionIndex);
        } else {
            return serviceKey;
        }
    }

    private String getGroup(String serviceKey) {
        if (StringUtils.isEmpty(serviceKey)) {
            throw new IllegalArgumentException("serviceKey must not be null");
        }

        // serviceKey serviceKey=group/interface:version
        int groupIndex = serviceKey.indexOf("/");
        if (groupIndex > 0) {
            return serviceKey.substring(0, groupIndex);
        } else {
            return null;
        }
    }

    private String getVersion(String serviceKey) {
        if (StringUtils.isEmpty(serviceKey)) {
            throw new IllegalArgumentException("serviceKey must not be null");
        }

        // serviceKey serviceKey=group/interface:version
        int versionIndex = serviceKey.indexOf(":");
        if (versionIndex > 0) {
            return serviceKey.substring(versionIndex + 1);
        } else {
            return null;
        }
    }

}