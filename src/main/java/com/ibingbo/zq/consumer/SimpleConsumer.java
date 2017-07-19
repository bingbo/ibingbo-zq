package com.ibingbo.zq.consumer;

/**
 * Created by bing on 17/7/19.
 */
public class SimpleConsumer extends AbstractConsumer {

    public SimpleConsumer() throws Exception {
    }

    public SimpleConsumer(String queueName) throws Exception {
        super(queueName);
    }

    public void receive(String message) throws Exception {

    }

    public void init() throws Exception {
        this.consume();
    }

}
