package com.gmall.payment.service.impl;

import com.gmall.bean.PaymentInfo;
import com.gmall.payment.mapper.PaymentInfoMapper;
import com.gmall.service.PaymentService;
import com.gmall.service.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
@com.alibaba.dubbo.config.annotation.Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        String orderSn = paymentInfo.getOrderSn();
        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn", orderSn);

        Connection connection = null;
        Session session = null;

        try {
            connection = activeMQUtil.getConnectionFactory()
                    .createConnection();
            session = connection.createSession(true,
                    Session.SESSION_TRANSACTED);
            // 支付成功后，引起的系统服务-》订单服务的更新-》库存服务-》物流
            // 调用MQ发送支付成功的消息
            Queue payment_success_queue = session.createQueue(
                    "PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(
                    payment_success_queue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());

            paymentInfoMapper.updateByExampleSelective(paymentInfo, e);
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
