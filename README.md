# ibingbo-zq
ibingbo-zookeeper-queue

> 通过zookeeper实现消息发送及订阅功能，模拟消息队列，了解异步消息机制，如RabbitQ,ActiveQ等原理

## 生产者

其逻辑由`AbstractProducer`抽象类封装，由`SimpleProducer`类实现并提供使用


### AbstractProducer

```java
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

```

### SimpleProducer

```java
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

```

## 消费者

> 其逻辑由`AbstractConsumer`抽象类封装，由`SimpleConsumer`类实现并提供使用

### AbstractConsumer

```java
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

```

### SimpleConsumer

```java
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

```

## 测试使用

```java
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

```