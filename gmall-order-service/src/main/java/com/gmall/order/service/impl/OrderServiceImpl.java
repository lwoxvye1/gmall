package com.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.OmsOrder;
import com.gmall.bean.OmsOrderItem;
import com.gmall.order.mapper.OmsOrderItemMapper;
import com.gmall.order.mapper.OmsOrderMapper;
import com.gmall.service.OrderService;
import com.gmall.service.util.ActiveMQUtil;
import com.gmall.service.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String generateTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:" + memberId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeKey, 60 * 15, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        try (Jedis jedis = redisUtil.getJedis()) {
            String tradeKey = "user:" + memberId + ":tradeCode";
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                return "success";
            } else {
                return "fail";
            }
        }
    }

    @Override
    @Transactional
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);

        String orderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            // 删除购物车数据
//            cartService.delCart();
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        return omsOrderMapper.selectOne(omsOrder);
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());

        OmsOrder omsOrderUpdate = new OmsOrder();
        omsOrder.setStatus(1);

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory()
                    .createConnection();
            session = connection.createSession(true,
                    Session.SESSION_TRANSACTED);

            Queue order_pay_queue = session.createQueue(
                    "ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(
                    order_pay_queue);
            MapMessage mapMessage = new ActiveMQMapMessage();

            omsOrderMapper.updateByExampleSelective(omsOrderUpdate, example);
            producer.send(mapMessage);

            session.commit();
        } catch (Exception ex){
            // 消息回滚
            try {
                session.rollback();
            } catch (JMSException exc) {
                exc.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }
}
