package com.ibingbo.zk.lock;

import java.util.concurrent.CountDownLatch;

/**
 * TestLock
 *
 * @author zhangbingbing
 * @date 18/1/18
 */
public class TestLock {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);

        for (int i=0;i<10;i++) {
            new MyThread(latch).start();
        }

        latch.await();
    }

    private static class MyThread extends Thread {
        private CountDownLatch latch;

        public MyThread(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            DistributedLock lock = new DistributedLock("lock0");
            try {

                lock.lock();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
            latch.countDown();
        }
    }

}
