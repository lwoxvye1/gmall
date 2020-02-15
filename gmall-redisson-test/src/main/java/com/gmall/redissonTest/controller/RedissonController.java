package com.gmall.redissonTest.controller;

import com.gmall.service.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

/**
 * Redis的分布式工具框架redisson
 */
@Controller
public class RedissonController {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/testRedisson")
    public String testRedisson(){
        Jedis jedis= redisUtil.getJedis();
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        try {
            String v = jedis.get("k");
            if (StringUtils.isBlank(v)){
                v = "1";
            }
            jedis.set("k", Integer.parseInt(v) + 1 + "");
            jedis.close();
        } finally {
            lock.unlock();
        }
        return "success";
    }
}
