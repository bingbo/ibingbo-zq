package com.ibingbo.zq.producer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibingbo.zq.queue.AbstractZKQueue;
import com.ibingbo.zq.util.ZooKeeperUtil;

/**
 * 生产者抽象类
 * Created by bing on 17/7/19.
 */
public abstract class AbstractProducer extends AbstractZKQueue implements Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProducer.class);

    public AbstractProducer() throws Exception {
    }

    /**
     * 向指定的队列发送消息
     * @param queue
     * @param bytes
     * @return
     * @throws Exception
     */
    public String produce(String queue, byte[] bytes) throws Exception {
        String path = QUEUE_ROOT + "/" + queue;
        ZooKeeperUtil.createPathIfNotExist(this.zooKeeper, path);
        String node =
                this.zooKeeper.create(path + "/", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        LOGGER.info("thread {} produce a message to queue: {} message data is: {}", Thread.currentThread().getName(),
                node, new String(bytes, "utf-8"));
        return node;
    }

    /**
     * 向指定的队列发送消息
     * @param queue
     * @param data
     * @return
     */
    public boolean sendMessage(String queue, String data) {
        try {
            byte[] bytes = data.getBytes();
            String node = this.produce(queue, bytes);
            return node != null;
        } catch (Exception e) {
            LOGGER.error("send message {} to {} fail", data, queue);
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}
