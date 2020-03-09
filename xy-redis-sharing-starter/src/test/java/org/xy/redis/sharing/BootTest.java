package org.xy.redis.sharing;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * @author wangxianyu
 */
@Slf4j
public class BootTest extends BaseTest {
    @Resource
    private RedisSharingTemplate sharingTemplate;

    @Test
    public void testSimple() {
        String key = "test-sharing", hashKey = "t1";
        //使用默认redis
        sharingTemplate.opsForHash().put(key, hashKey, "test");
        //根据sharingArgs自动选择redis
        sharingTemplate.opsForHash(1).put(key, hashKey, "test");
        //使用默认redis
        System.out.println(sharingTemplate.opsForHash(null).get(key, hashKey));
        //如无匹配规则的redis使用默认redis
        System.out.println(sharingTemplate.opsForHash(1111).get(key, hashKey));
    }
}
