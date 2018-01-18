package com.ibingbo.zk.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibingbo.zq.util.ZooKeeperUtil;

/**
 * DistributedLock
 *
 * @author zhangbingbing
 * @date 18/1/18
 */
public class DistributedLock implements Lock,Watcher{
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLock.class);

    private ZooKeeper zooKeeper;
    private static final String LOCK_ROOT = "/locks";
    private static final String SPLIT_LOCK_STR = "_lock_";
    private static final int TIMEOUT = 30000;
    private String lockName;
    private String waitNode;
    private String myZnode;
    private CountDownLatch latch;

    public DistributedLock(String lockName) {
        this.lockName = lockName;
        try {
            this.zooKeeper = ZooKeeperUtil.getInstance();
            Stat stat = this.zooKeeper.exists(LOCK_ROOT, false);
            if (stat == null) {
                this.zooKeeper.create(LOCK_ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void lock() {
        try {
            if (this.tryLock()) {
                System.out.println("Thread " + Thread.currentThread().getId() + " " + myZnode + " get lock true");
                return;
            } else {
                waitForLock(waitNode, TIMEOUT);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private boolean waitForLock(String waitNode, long timeout) throws KeeperException, InterruptedException {
        Stat stat = this.zooKeeper.exists(LOCK_ROOT + "/" + waitNode, true);
        if (stat != null) {
            System.out
                    .println("Thread " + Thread.currentThread().getId() + " waiting for " + LOCK_ROOT + "/" + waitNode);
            this.latch = new CountDownLatch(1);
            this.latch.await(timeout, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    @Override
    public boolean tryLock() {
        try {
            if (this.lockName.contains(SPLIT_LOCK_STR)) {
                throw new Exception("lockName can not contains " + SPLIT_LOCK_STR);
            }
            this.myZnode =
                    this.zooKeeper.create(LOCK_ROOT + "/" + this.lockName + SPLIT_LOCK_STR, new byte[0], ZooDefs.Ids
                            .OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(this.myZnode + " is created");

            List<String> subNodes = this.zooKeeper.getChildren(LOCK_ROOT, false);
            List<String> lockObjNodes = new ArrayList<>();
            for (String node : subNodes) {
                String _node = node.split(SPLIT_LOCK_STR)[0];
                if (this.lockName.equals(_node)) {
                    lockObjNodes.add(node);
                }
            }

            Collections.sort(lockObjNodes);

            if (this.myZnode.equals(LOCK_ROOT + "/" + lockObjNodes.get(0))) {
                System.out.println(this.myZnode + " == " + lockObjNodes.get(0));
                return true;
            }

            String subMyZnode = this.myZnode.substring(myZnode.lastIndexOf("/") + 1);
            this.waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode));
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(this.waitNode, time);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void unlock() {
        try {
            System.out.println("unlock " + this.myZnode);
            this.zooKeeper.delete(this.myZnode, -1);
            this.myZnode = null;
            this.zooKeeper.close();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        if (this.latch != null) {
            this.latch.countDown();
        }
    }
}
