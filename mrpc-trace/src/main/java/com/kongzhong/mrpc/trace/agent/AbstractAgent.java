package com.kongzhong.mrpc.trace.agent;

import com.github.kristofa.brave.AbstractSpanCollector;
import com.twitter.zipkin.gen.Span;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author biezhi
 * @date 2017/11/23
 */
public class AbstractAgent {

    protected AbstractSpanCollector collector;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, r -> {
                Thread worker = new Thread(r);
                worker.setName("TRACE-AGENT-WORKER");
                worker.setDaemon(true);
                return worker;
            });

    public void send(final List<Span> spans) {
        if (spans != null && !spans.isEmpty()) {
            executor.submit(() -> {
                for (Span span : spans) {
                    collector.collect(span);
                }
                collector.flush();
            });
        }
    }
}
