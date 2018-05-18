package com.kongzhong.mrpc.event;

import com.kongzhong.mrpc.enums.EventType;
import com.kongzhong.mrpc.model.RpcContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 事件管理器
 * <p>
 * Created by biezhi on 03/07/2017.
 */
public class EventManager {

    private Map<EventType, List<EventListener>> listenerMap;

    private EventManager() {
        this.listenerMap = Stream.of(EventType.values()).collect(Collectors.toMap(v -> v, v -> new LinkedList<>()));
    }

    public void addEventListener(EventType type, EventListener listener) {
        listenerMap.get(type).add(listener);
    }

    public void fireEvent(EventType type, Event event) {
        listenerMap.get(type).stream()
                .forEach(listener -> listener.trigger(event));
    }

    private static final class EventManagerHolder {
        private static final EventManager INSTANCE = new EventManager();
    }

    public static EventManager me() {
        return EventManagerHolder.INSTANCE;
    }

}
