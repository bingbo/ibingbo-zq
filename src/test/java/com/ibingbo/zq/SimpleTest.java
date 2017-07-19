package com.ibingbo.zq;

import com.ibingbo.zq.consumer.SimpleConsumer;
import com.ibingbo.zq.producer.SimpleProducer;

/**
 * Created by bing on 17/7/19.
 */
public class SimpleTest {
    public static void main(String[] args) throws Exception {
        final SimpleProducer producer = new SimpleProducer();
        final SimpleConsumer consumer = new SimpleConsumer();

        /**
         * 启动生产者线程发送消息
         */
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                        producer.sendMessage("message" + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "produce-thread").start();

        /**
         * 启动消费者线和消费消息
         */
        new Thread(new Runnable() {
            public void run() {
                try {
                    consumer.init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
