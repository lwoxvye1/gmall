package com.gmall.service.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.ConnectionFactory;

public class ActiveMQUtil {
    PooledConnectionFactory pooledConnectionFactory = null;

    public ConnectionFactory init(String brokeUrl){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokeUrl);
        pooledConnectionFactory = new PooledConnectionFactory(factory);
        pooledConnectionFactory.setReconnectOnException(true);
        pooledConnectionFactory.setMaxConnections(5);
        pooledConnectionFactory.setExpiryTimeout(10000);
        return pooledConnectionFactory;
    }

    public ConnectionFactory getConnectionFactory(){
        return pooledConnectionFactory;
    }
}
