package com.ibingbo.zk.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

/**
 * DistributedLock2
 *
 * @author zhangbingbing
 * @date 18/1/18
 */
public class DistributedLock2 {
    public static void main(String[] args) {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(10, 5000));
        client.start();

        System.out.println(client.getState());

        System.out.println("zk client start successfully");

        InterProcessMutex lock = new InterProcessMutex(client, "/locks/lock0");
        for (int i=0;i<10;i++) {
            new Thread(() -> {
                try {
                    String name = Thread.currentThread().getName();
                    if (lock.acquire(10 * 1000, TimeUnit.SECONDS)) {
                        System.out.println(name + " hold lock");
                        System.out.println(client.getChildren().forPath("/locks/lock0"));

                        Thread.sleep(5000L);
                        System.out.println(name + " release lock");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
