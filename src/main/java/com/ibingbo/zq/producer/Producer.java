package com.ibingbo.zq.producer;

/**
 * Created by bing on 17/7/19.
 */
public interface Producer {
    String produce(String queue, byte[] bytes) throws Exception;
}
