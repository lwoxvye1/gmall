package com.gmall.service;

import com.gmall.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String generateTradeCode(String memberId);

    String checkTradeCode(String memberId, String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
