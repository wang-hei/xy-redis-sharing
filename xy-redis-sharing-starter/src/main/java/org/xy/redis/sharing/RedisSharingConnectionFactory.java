package org.xy.redis.sharing;

import org.xy.redis.sharing.boot.properties.RedisSharingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author wangxianyu
 */
@Slf4j
public class RedisSharingConnectionFactory implements InitializingBean, DisposableBean {
    private Map<String, JedisConnectionFactory> factoryCache = new HashMap<>();
    private SharingHandler<Integer, String> sharingHandler;

    public RedisSharingConnectionFactory(final RedisSharingProperties sharingProperties) {
        init(sharingProperties);
    }

    private void init(final RedisSharingProperties sharingProperties) {
        try {
            sharingHandler = new AbstractSharingHandler<Integer, String>() {
                @Override
                public synchronized JedisConnectionFactory addNode(final String nodeName, final RedisSharingProperties.RedisPropertiesNode nodeProperties) {
                    Assert.isTrue(!inited, " handler is inited can not be add node!");
                    Assert.hasText(nodeName, "nodeName can not be empty!");
                    Assert.notNull(nodeProperties, "nodeProperties can not be null!");
                    JedisConnectionFactory rs = null;
                    if (!nodeCache.containsKey(nodeName)) {
                        try {
                            rs = applyProperties(createJedisConnectionFactory(nodeProperties), nodeProperties);
                            nodeCache.put(nodeName, new StringRedisTemplate(rs));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            if (null != rs) {
                                rs.destroy();
                            }
                        }
                    }
                    Arrays.stream(nodeProperties.getRule().split(",")).forEach(ruleTemp -> {
                        Integer rule = Integer.valueOf(ruleTemp);
                        if (sharingCache.containsKey(rule)) {
                            throw new RuntimeException(String.format("duplicate sharing rule!{rule:%s} ", rule));
                        } else {
                            sharingCache.put(rule, nodeName);
                        }
                    });
                    return rs;
                }

                @Override
                public StringRedisTemplate choice(final Integer sharingArgs, final StringRedisTemplate defaultTemplate) {
                    return super.getTemplate(this.sharingCache.get(sharingArgs), defaultTemplate);
                }
            };
            sharingProperties.getNode().forEach((k, v) -> {
                JedisConnectionFactory factoryTemp = sharingHandler.addNode(k, v);
                if (null != factoryTemp && !factoryCache.containsKey(k)) {
                    factoryCache.put(k, factoryTemp);
                }
            });
            sharingHandler.ready();
            log.info("[RedisSharing] RedisSharingConnectionFactory -> inited!", factoryCache.keySet());
        } catch (Exception e) {
            this.destroy(e);
        }
    }

    private JedisConnectionFactory createJedisConnectionFactory(final RedisProperties properties) {
        JedisPoolConfig poolConfig = properties.getPool() != null ? this.jedisPoolConfig(properties) : new JedisPoolConfig();
        if (this.getSentinelConfig(properties) != null) {
            return new JedisConnectionFactory(this.getSentinelConfig(properties), poolConfig);
        } else {
            RedisClusterConfiguration configuration = this.getClusterConfiguration(properties);
            return configuration != null ? new JedisConnectionFactory(configuration, poolConfig) : new JedisConnectionFactory(poolConfig);
        }
    }

    /**
     * Create a {@link RedisClusterConfiguration} if necessary.
     *
     * @return {@literal null} if no cluster settings are set.
     */
    private RedisClusterConfiguration getClusterConfiguration(final RedisProperties properties) {
        if (properties.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = properties.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        return config;
    }

    private JedisPoolConfig jedisPoolConfig(final RedisProperties properties) {
        JedisPoolConfig config = new JedisPoolConfig();
        RedisProperties.Pool props = properties.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis((long) props.getMaxWait());
        return config;
    }

    private RedisSentinelConfiguration getSentinelConfig(final RedisProperties properties) {
        RedisProperties.Sentinel sentinelProperties = properties.getSentinel();
        if (sentinelProperties != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(sentinelProperties.getMaster());
            config.setSentinels(createSentinels(sentinelProperties));
            return config;
        }
        return null;
    }

    private List<RedisNode> createSentinels(final RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : StringUtils.commaDelimitedListToStringArray(sentinel.getNodes())) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel " + "property '" + node + "'", ex);
            }
        }
        return nodes;
    }

    private JedisConnectionFactory applyProperties(final JedisConnectionFactory factory, final RedisProperties properties) {
        configureConnection(factory, properties);
        if (properties.isSsl()) {
            factory.setUseSsl(true);
        }
        factory.setDatabase(properties.getDatabase());
        if (properties.getTimeout() > 0) {
            factory.setTimeout(properties.getTimeout());
        }
        return factory;
    }

    private void configureConnection(final JedisConnectionFactory factory, final RedisProperties properties) {
        if (StringUtils.hasText(properties.getUrl())) {
            configureConnectionFromUrl(factory, properties);
        } else {
            factory.setHostName(properties.getHost());
            factory.setPort(properties.getPort());
            if (properties.getPassword() != null) {
                factory.setPassword(properties.getPassword());
            }
        }
    }

    private void configureConnectionFromUrl(final JedisConnectionFactory factory, final RedisProperties properties) {
        String url = properties.getUrl();
        if (url.startsWith("rediss://")) {
            factory.setUseSsl(true);
        }
        try {
            URI uri = new URI(url);
            factory.setHostName(uri.getHost());
            factory.setPort(uri.getPort());
            if (uri.getUserInfo() != null) {
                String password = uri.getUserInfo();
                int index = password.lastIndexOf(":");
                if (index >= 0) {
                    password = password.substring(index + 1);
                }
                factory.setPassword(password);
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed 'spring.redis.url' " + url, ex);
        }
    }

    @Override
    public void destroy() throws Exception {
        factoryCache.values().forEach(JedisConnectionFactory::destroy);
        log.info("[RedisSharing] RedisSharingConnectionFactory -> destroy!", factoryCache.keySet());
    }

    private void destroy(Exception e) {
        try {
            this.destroy();
            throw e;
        } catch (Exception de) {
            throw new RuntimeException(de);
        }
    }

    public SharingHandler<Integer, String> getSharingHandler() {
        return sharingHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[RedisSharing] RedisSharingConnectionFactory -> afterPropertiesSet!", factoryCache.keySet());
        factoryCache.values().forEach(JedisConnectionFactory::afterPropertiesSet);
    }
}
