package com.ibingbo.zq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bing on 17/7/19.
 */
public class SimpleProducer extends AbstractProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProducer.class);

    private String queueName = "test";

    public SimpleProducer() throws Exception {
    }

    public SimpleProducer(String queueName) throws Exception {
        this.queueName = queueName;
    }

    public boolean sendMessage(String data) {
        return this.sendMessage(this.queueName, data);
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
