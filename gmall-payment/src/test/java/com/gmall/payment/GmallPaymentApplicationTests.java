package com.gmall.payment;

import com.gmall.service.util.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.jms.*;

@SpringBootTest
class GmallPaymentApplicationTests {

    @Test
    void Provider() {
        ConnectionFactory connect = new ActiveMQConnectionFactory(
                "tcp://120.55.94.181:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(true,
                    Session.SESSION_TRANSACTED); // 开启事务
            Queue testQueue = session.createQueue("TEST1"); // 队列模式的消息
//            Topic t = session.createTopic("") // 订阅模式的消息

            MessageProducer producer = session.createProducer(testQueue);
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("今天天气真好！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            session.commit();
            connection.close();
        } catch (JMSException e){
            e.printStackTrace();
        }
    }

    @Test
    void Consumer(){
        ConnectionFactory connect = new ActiveMQConnectionFactory(
                ActiveMQConnectionFactory.DEFAULT_USER,
                ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                "tcp://120.55.94.181:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            Destination testQueue = session.createQueue("TEST1");

            MessageConsumer consumer = session.createConsumer(testQueue);
            consumer.setMessageListener(message -> {
                if (message instanceof  TextMessage){
                    try {
                        String text = ((TextMessage) message).getText();
                        System.out.println(text);
                    } catch (JMSException e){
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Test
    void test() throws JMSException {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();

        System.out.println(connection);
    }
}
