package org.xy.redis.sharing;

import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

/**
 * @author wangxianyu
 */
public final class RedisSharingTemplate {
    private boolean hasHandler;
    private StringRedisTemplate defaultTemplate;
    private SharingHandler<Integer, String> sharingHandler;

    public RedisSharingTemplate(final StringRedisTemplate defaultTemplate) {
        Assert.notNull(defaultTemplate, " defaultTemplate can not bee null!");
        hasHandler = false;
        this.defaultTemplate = defaultTemplate;
    }

    public RedisSharingTemplate(final SharingHandler<Integer, String> sharingHandler, final StringRedisTemplate defaultTemplate) {
        Assert.notNull(sharingHandler, " sharingHandler can not bee null!");
        Assert.notNull(defaultTemplate, " defaultTemplate can not bee null!");
        hasHandler = true;
        this.sharingHandler = sharingHandler;
        this.defaultTemplate = defaultTemplate;
    }

    @SuppressWarnings("unchecked")
    public byte[] rawKey(final StringRedisTemplate redisTemplate, final Object key) {
        Assert.notNull(key, "non null key required");
        RedisSerializer keySerializer = keySerializer(redisTemplate);
        if (keySerializer == null && key instanceof byte[]) {
            return (byte[]) key;
        }
        return keySerializer.serialize(key);
    }

    private RedisSerializer keySerializer(final StringRedisTemplate redisTemplate) {
        return redisTemplate.getKeySerializer();
    }

    /**
     * @return never {@literal null}.
     */
    public StringRedisTemplate sharingForArgs(Integer sharingArgs) {
        return hasHandler ? sharingHandler.choice(sharingArgs, defaultTemplate) : defaultTemplate;
    }

    /**
     * @return never {@literal null}.
     */
    public StringRedisTemplate getDefaultTemplate() {
        return defaultTemplate;
    }

    /**
     * @return never {@literal null}.
     */
    public ValueOperations<String, String> opsForValue(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForValue();
    }

    /**
     * @return never {@literal null}.
     */
    public ListOperations<String, String> opsForList(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForList();
    }

    /**
     * @return never {@literal null}.
     */
    public SetOperations<String, String> opsForSet(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForSet();
    }

    /**
     * @return never {@literal null}.
     */
    public ZSetOperations<String, String> opsForZSet(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForZSet();
    }

    /**
     * @return never {@literal null}.
     */
    public HyperLogLogOperations<String, String> opsForHyperLogLog(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForHyperLogLog();
    }

    /**
     * @return never {@literal null}.
     */
    public <HK, HV> HashOperations<String, HK, HV> opsForHash(Integer sharingArgs) {
        return sharingForArgs(sharingArgs).opsForHash();
    }

    /**
     * defaultTemplate
     * ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ----
     */
    public ValueOperations<String, String> opsForValue() {
        return defaultTemplate.opsForValue();
    }

    public ListOperations<String, String> opsForList() {
        return defaultTemplate.opsForList();
    }

    public SetOperations<String, String> opsForSet() {
        return defaultTemplate.opsForSet();
    }

    public ZSetOperations<String, String> opsForZSet() {
        return defaultTemplate.opsForZSet();
    }

    public HyperLogLogOperations<String, String> opsForHyperLogLog() {
        return defaultTemplate.opsForHyperLogLog();
    }

    public <HK, HV> HashOperations<String, HK, HV> opsForHash() {
        return defaultTemplate.opsForHash();
    }

}
