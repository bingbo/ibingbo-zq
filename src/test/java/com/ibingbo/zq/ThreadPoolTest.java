package com.ibingbo.zq;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bing on 17/7/19.
 */
@SuppressWarnings("ALL")
public class ThreadPoolTest {
    public static void main(String[] args) throws Exception{
        final Random random = new Random();
        ExecutorService service = Executors.newFixedThreadPool(10);
        CompletionService<String> completionService = new ExecutorCompletionService<String>(service);
        for (int i=0;i<50;i++) {
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
