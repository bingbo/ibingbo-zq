package com.ibingbo.zq.queue;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibingbo.zq.util.ZooKeeperUtil;

/**
 * zookeeper队列抽象类，封装了根路径及zk实例，以便消费者和生产者使用
 * Created by bing on 17/7/19.
 */
public abstract class AbstractZKQueue implements ZKQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZKQueue.class);

    protected ZooKeeper zooKeeper;
    protected String QUEUE_ROOT = "/QUEUE";

    public AbstractZKQueue() throws Exception {
        this.zooKeeper = ZooKeeperUtil.getInstance();
        ZooKeeperUtil.createPathIfNotExist(this.zooKeeper, QUEUE_ROOT);
    }

}
