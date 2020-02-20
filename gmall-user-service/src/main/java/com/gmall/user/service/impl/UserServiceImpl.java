package com.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.UmsMember;
import com.gmall.bean.UmsMemberReceiveAddress;
import com.gmall.service.UserService;
import com.gmall.service.util.RedisUtil;
import com.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.gmall.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
//        List<UmsMember> umsMembers = userMapper.selectAllUser();
        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example e = new Example(UmsMemberReceiveAddress.class);
        e.createCriteria().andEqualTo("memberId", memberId);

        return umsMemberReceiveAddressMapper.selectByExample(e);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 偷懒
            if (jedis != null) {
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + ":info");
                if (StringUtils.isNotBlank(umsMemberStr)) {
                    return JSON.parseObject(umsMemberStr, UmsMember.class);
                }
            }

            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMemberFromDb.getPassword() + ":info",
                        60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + memberId + ":token", 60 * 60 * 2, token);
        jedis.close();
    }

    @Override
    public void addOauthUser(UmsMember umsMember){
        userMapper.insertSelective(umsMember);
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        return userMapper.selectOne(umsCheck);
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        return userMapper.selectOne(umsMember);
    }
}
