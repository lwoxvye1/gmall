package com.gmall.seckill.controller;

import com.gmall.service.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("/kill")
    @ResponseBody
    public String kill(){
        String memberId = "1";
        Jedis jedis = redisUtil.getJedis();

        jedis.watch("106");
        int stock = Integer.parseInt(jedis.get("106"));
        if (stock > 0) {
            Transaction multi = jedis.multi();
            multi.incrBy("106", -1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0){
                System.out.println("当前库存剩余数量" + stock);
            }
        }
        jedis.close();
        return "1";
    }

    @RequestMapping("/secKill")
    @ResponseBody
    public String secKill(){
        RSemaphore semaphore = redissonClient.getSemaphore("106");
        boolean b = semaphore.tryAcquire();
        if (b){
            // 发出订单消息
        } else {
            System.out.println("抢购失败");
        }

        return "1";
    }
}
