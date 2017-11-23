package com.kongzhong.mrpc.trace.agent;

import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.kongzhong.mrpc.trace.SimpleMetricsHandler;
import com.kongzhong.mrpc.trace.agent.AbstractAgent;
import com.kongzhong.mrpc.trace.collector.HttpSpanCollector;

/**
 * HttpAgent
 */
public class HttpAgent extends AbstractAgent {

    public HttpAgent(String server) {
        SpanCollectorMetricsHandler metrics = new SimpleMetricsHandler();
        // set flush interval to 0 so that tests can drive flushing explicitly
        HttpSpanCollector.Config config =
                HttpSpanCollector.Config.builder()
                        .compressionEnabled(true)
                        .flushInterval(0)
                        .build();

        super.collector = HttpSpanCollector.create(server, config, metrics);
    }

}