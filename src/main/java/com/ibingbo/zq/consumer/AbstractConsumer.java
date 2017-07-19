package com.ibingbo.zq.consumer;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibingbo.zq.queue.AbstractZKQueue;

/**
 * 生产者抽象类
 * Created by bing on 17/7/19.
 */
public abstract class AbstractConsumer extends AbstractZKQueue implements Consumer, Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    private Object mutex = new Object();
    private String queue = "test";

    public AbstractConsumer(String queue) throws Exception {
        this.queue = queue;
    }

    protected AbstractConsumer() throws Exception {
    }

    /**
     * 循环消费消息
     *
     * @throws Exception
     */
    public void consume() throws Exception {
        String path = QUEUE_ROOT + "/" + queue;
        List<String> nodes = null;
        byte[] result = null;
        Stat stat = null;
        do {
            synchronized(mutex) {
                nodes = this.zooKeeper.getChildren(path, this);

                if (nodes == null || nodes.size() == 0) {
                    mutex.wait();
                } else {
                    SortedSet<String> sortedNode = new TreeSet<String>();
                    for (String node : nodes) {
                        sortedNode.add(path + "/" + node);
                    }

                    String first = sortedNode.first();
                    result = this.zooKeeper.getData(first, false, stat);
                    this.zooKeeper.delete(first, -1);
                    String data = new String(result, "utf-8");
                    LOGGER.info("thread {} consume a message from queue: {}, message data is : {}", Thread
                            .currentThread().getName(), first, data);
                    this.receive(data);
                }
            }
        } while (true);
    }

    /**
     * 监控节点变动回调
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        synchronized(mutex) {
            mutex.notify();
        }
    }

    /**
     * 消息处理回调
     *
     * @param message
     *
     * @throws Exception
     */
    public abstract void receive(String message) throws Exception;

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
