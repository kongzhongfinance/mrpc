package com.kongzhong.mrpc.event;

/**
 * 事件监听器
 * <p>
 * Created by biezhi on 03/07/2017.
 */
@FunctionalInterface
public interface EventListener {

    /**
     * 触发事件
     *
     * @param event
     */
    void trigger(Event event);

}
