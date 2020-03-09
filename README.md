## 简介

    基于 spring-data-redis 实现 Redis Set化

## 最新版本

```xml
<dependency>
    <groupId>org.xy</groupId>
    <artifactId>xy-redis-sharing-starter</artifactId>
    <version>1.0.1-RELEASE</version>
</dependency>
```

## 使用

#### 配置sharing规则
``` yml
redis:
# set 节点配置
  sharing:
    enabled: true
    node:
      one: # 节点1[名称随便]
        rule: 1,2,3 # 规则表达式','分割[格式为整型，不可重复]
        timeout: 1000
        cluster:
          nodes: 127.0.0.1:6001
          maxRedirects: 6
        pool:
          max-active: 1024 #最大连接数
          max-idle: 200 #最大空闲连接数
          max-wait: -1 #获取连接最大等待时间 ms
      two: # 节点2
        rule: 4,5,6
        timeout: 1000
        cluster:
          nodes: 127.0.0.1:7001
          maxRedirects: 6
        pool:
          max-active: 1024
          max-idle: 200
          max-wait: -1 
spring:
# 默认redis
  redis:
    timeout: 1000
    cluster:
      nodes: 127.0.0.1:8001
      maxRedirects: 6
    pool:
      max-active: 1024
      max-idle: 200
      max-wait: -1
```
#### 使用伪代码
``` java
String key = "test-sharing", hashKey = "t1";
//使用默认redis
sharingTemplate.opsForHash().put(key, hashKey,"test");
//根据sharingArgs自动选择redis
sharingTemplate.opsForHash(1).put(key, hashKey,"test");
//使用默认redis
System.out.println(sharingTemplate.opsForHash(null).get(key, hashKey));
//如无匹配规则的redis使用默认redis
System.out.println(sharingTemplate.opsForHash(1111).get(key, hashKey));
```
