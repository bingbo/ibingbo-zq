package com.ibingbo.zq.producer;

/**
 * Created by bing on 17/7/19.
 */
public interface Producer {
    String produce(String queue, byte[] bytes) throws Exception;

    /**
     * @since 1.8
     * @return
     */
    default String version() {
        return "1.0.0";
    }
}
