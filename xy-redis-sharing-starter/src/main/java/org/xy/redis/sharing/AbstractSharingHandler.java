package org.xy.redis.sharing;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangxianyu
 */
abstract class AbstractSharingHandler<ST, CKT> implements SharingHandler<ST, CKT> {
    protected volatile boolean inited = false;
    protected Map<ST, CKT> sharingCache = new HashMap<>();
    protected Map<CKT, StringRedisTemplate> nodeCache = new ConcurrentHashMap<>();

    /**
     * @return never {@literal null}.
     */
    protected StringRedisTemplate getTemplate(CKT cacheKey, StringRedisTemplate defaultTemplate) {
        if (null == cacheKey) {
            return defaultTemplate;
        }
        Assert.notNull(defaultTemplate, " defaultTemplate can not be null!");
        return Optional.ofNullable(nodeCache.get(cacheKey)).orElse(defaultTemplate);
    }


    protected StringRedisTemplate removeTemplate(CKT cacheKey) {
        Assert.notNull(cacheKey, " cacheKey can not be null!");
        return nodeCache.remove(cacheKey);
    }

    @Override
    public void ready() {
        inited = true;
    }
}