package org.xy.redis.sharing.boot.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangxianyu
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "redis.sharing")
public class RedisSharingProperties {
    private boolean enabled;
    private Map<String, RedisPropertiesNode> node = new HashMap<>();

    @Setter
    @Getter
    public static class RedisPropertiesNode extends RedisProperties {
        private String rule;
    }
}
