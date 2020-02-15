package com.gmall.manager;


import com.gmall.bean.PmsSkuImage;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.manager.mapper.PmsSkuImageMapper;
import com.gmall.manager.mapper.PmsSkuInfoMapper;
import com.gmall.service.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;


@SpringBootTest
class GmallManagerServiceApplicationTests {
    @Autowired
    RedisUtil redisUtil;

    @Test
    public void contextLoads() {
        Jedis jedis = redisUtil.getJedis();
        System.out.println(jedis);
    }

}
