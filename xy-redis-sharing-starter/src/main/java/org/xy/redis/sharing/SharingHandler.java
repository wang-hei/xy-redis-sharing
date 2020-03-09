package org.xy.redis.sharing;

import org.xy.redis.sharing.boot.properties.RedisSharingProperties;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author wangxianyu
 */
interface SharingHandler<ST, CKT> {

    /**
     * @return never {@literal null}.
     */
    StringRedisTemplate choice(final ST sharingArgs, final StringRedisTemplate defaultTemplate);

    JedisConnectionFactory addNode(final CKT nodeName, final RedisSharingProperties.RedisPropertiesNode nodeProperties);

    void ready();
}
