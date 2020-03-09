package org.xy.redis.sharing;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xy.redis.sharing.boot.RedisSharingAutoConfigure;

/**
 * @author wangxianyu
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisSharingAutoConfigure.class)
public abstract class BaseTest {
}
