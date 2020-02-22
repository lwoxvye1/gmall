package com.gmall.payment.mq;

import com.gmall.bean.PaymentInfo;
import com.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",
            containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException{
        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count = 0;
        if (mapMessage.getString("count") != null) {
            count = Integer.parseInt("" + mapMessage.getInt("count"));
        }
        // 调用paymentService的支付宝检查接口
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
        if (resultMap == null || resultMap.isEmpty()){
            if (count > 0){
                count --;
                paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
            }
        } else {
            String trade_status = (String) resultMap.get("trade_status");

            // 根据查询的支付状态结果，判断是否进行下一次的延迟任务还是支付成功更新数据和后续任务
            if ("TRADE_SUCCESS".equals(trade_status)) {
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String)resultMap.get("trade_no"));
                paymentInfo.setCallbackContent((String)resultMap.get("call_back_content"));
                paymentInfo.setCallbackTime(new Date());
                paymentService.updatePayment(null);
            } else {
                if (count > 0){
                    count --;
                    paymentService.sendDelayPaymentResultCheckQueue(out_trade_no, count);
                }
            }
        }
    }
}
