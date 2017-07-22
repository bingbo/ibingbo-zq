package com.ibingbo.zq;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by bing on 17/7/19.
 */
@SuppressWarnings("ALL")
public class ThreadPoolTest {
    public static void main(String[] args) throws Exception {
        final Random random = new Random();
        ExecutorService service = Executors.newFixedThreadPool(10);
        BlockingQueue workerQueue = new LinkedBlockingQueue<Runnable>();
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        };
        ExecutorService service1 = new ThreadPoolExecutor(1, 10, 2, TimeUnit.SECONDS, workerQueue, Executors
                .defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        CompletionService<String> completionService = new ExecutorCompletionService<String>(service1);
        for (int i = 0; i < 50; i++) {
            completionService.submit(new Callable<String>() {
                public String call() throws Exception {
                    Thread.sleep(random.nextInt(5000));
                    return Thread.currentThread().getName();
                }
            });
        }

        int completionTask = 0;
        while (completionTask < 50) {
            Future<String> result = completionService.take();
            System.out.println("result: " + result.get());
            completionTask++;
        }
        System.out.println(completionTask + " task done !");
        service.shutdown();

        Map<String, Object> map = new HashMap<String, Object>();
        map.putIfAbsent("name", "bill");
    }
}
