package org.xy.redis.sharing.boot;

import org.xy.redis.sharing.RedisSharingConnectionFactory;
import org.xy.redis.sharing.RedisSharingTemplate;
import org.xy.redis.sharing.boot.properties.RedisSharingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author wangxianyu
 */
@Configuration
@Import(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "redis.sharing", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({RedisSharingProperties.class})
public class RedisSharingAutoConfigure {

    @Bean
    public RedisSharingConnectionFactory getRedisSharingConnectionFactory(@Autowired RedisSharingProperties sharingProperties) {
        return new RedisSharingConnectionFactory(sharingProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RedisSharingTemplate.class)
    public RedisSharingTemplate getStringRedisSharingTemplate(@Autowired RedisSharingConnectionFactory sharingConnectionFactory, @Autowired StringRedisTemplate stringRedisTemplate) {
        return new RedisSharingTemplate(sharingConnectionFactory.getSharingHandler(), stringRedisTemplate);
    }
}
