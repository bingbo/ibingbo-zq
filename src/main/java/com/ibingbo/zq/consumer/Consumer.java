package com.ibingbo.zq.consumer;

/**
 * Created by bing on 17/7/19.
 */
public interface Consumer {
    void consume() throws Exception;

    default String version() {
        return "1.0.0";
    }
}
