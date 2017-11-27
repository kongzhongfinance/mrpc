package com.kongzhong.mrpc.trace.agent;

import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.kongzhong.mrpc.trace.SimpleMetricsHandler;
import com.kongzhong.mrpc.trace.collector.KafkaSpanCollector;

/**
 * KafkaAgent
 */
public class KafkaAgent extends AbstractAgent {

    public KafkaAgent(String server) {
        SpanCollectorMetricsHandler metrics   = new SimpleMetricsHandler();
        KafkaSpanCollector.Config   config    = KafkaSpanCollector.Config.builder(server).flushInterval(0).build();
        this.collector = new KafkaSpanCollector(config, metrics);
    }

}