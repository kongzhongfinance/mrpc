package com.kongzhong.mrpc.metric;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MetricClient {

    private final static String BIZ_PREFIX = "apps";

    private final static double DEFAULT_SAMPLE_RATE = 1.0;

    private static StatsDClient client = new NoOpStatsDClient();

    @Setter
    @Getter
    private static boolean enabled = true;

    public static void start(String url) {
        URI uri = URI.create("statsd://" + url);
        client = new NonBlockingStatsDClient(null, uri.getHost(), uri.getPort());
    }

    public static void stop() {
        StatsDClient tmp = client;
        client = new NoOpStatsDClient();
        tmp.stop();
    }

    private String prefix = BIZ_PREFIX;
    private String name = null;
    private String shortName = null;

    /**
     * Configures {@link MetricClient} with the specified prefix
     *
     * @param prefix the prefix of the metric name
     */
    public MetricClient prefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix shouldn't be null or empty!");
        }

        if (name != null) {
            throw new IllegalStateException("Prefix shouldn't be set after name!");
        }

        this.prefix = prefix;
        return this;
    }

    /**
     * Configures {@link MetricClient} with the specified aspects
     *
     * @param aspects the aspects of the metric name
     */
    public MetricClient name(String... aspects) {
        if (aspects == null || aspects.length == 0) {
            throw new IllegalArgumentException("Aspects shouldn't be null or empty!");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aspects.length; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(escape(aspects[i]));
        }
        this.shortName = sb.toString();

        String team = "default";
        String appId = "1000";
        this.name = new StringBuilder().append(escape(prefix)).append('.').append(escape(team != null ? team : "no_team")).append('.')
                .append(escape(appId != null ? appId : "no_appId")).append('.').append(this.shortName).toString();
        return this;
    }

    private String escape(String aspect) {
        return String.valueOf(aspect).trim().replace('.', '_').replace(':', '-');
    }

    private void recordTraceEvent(String type, long value, double sampleRate) {
        if (BIZ_PREFIX.equals(prefix)) {
            Map<String, String> tags = null;
            if (sampleRate != DEFAULT_SAMPLE_RATE) {
                tags = new HashMap<>();
                tags.put("sample-rate", Double.toString(sampleRate));
            }
//            Trace.logEvent(type, shortName, Constants.SUCCESS, Long.toString(value), tags);
        }
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param delta the amount to adjust the counter by
     */
    public void recordCount(long delta) {
        recordCount(delta, DEFAULT_SAMPLE_RATE);
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param delta      the amount to adjust the counter by
     * @param sampleRate the sampling rate being employed. For example, a rate of 0.1 would tell StatsD that this counter is being sent sampled every 1/10th of the
     *                   time.
     */
    public void recordCount(long delta, double sampleRate) {
        if (isEnabled()) {
            client.count(name, delta, sampleRate);
        }
        recordTraceEvent("metric-count", delta, sampleRate);
    }

    /**
     * Adjusts the specified counter by +1.
     */
    public void recordIncrement() {
        recordCount(1);
    }

    /**
     * Adjusts the specified counter by -1.
     */
    public void recordDecrement() {
        recordCount(-1);
    }

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param value the new reading of the gauge
     */
    public void recordGaugeValue(long value) {
        if (isEnabled()) {
            client.recordGaugeValue(name, value);
        }
    }

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * @param value the new reading of the gauge
     */
    public void recordGaugeValue(double value) {
        if (isEnabled()) {
            client.recordGaugeValue(name, value);
        }
    }

    /**
     * Records a change in the value of the specified named gauge.
     *
     * @param delta the +/- delta to apply to the gauge
     */
    public void recordGaugeDelta(long delta) {
        if (isEnabled()) {
            client.recordGaugeDelta(name, delta);
        }
    }

    /**
     * Records a change in the value of the specified named gauge.
     *
     * @param delta the +/- delta to apply to the gauge
     */
    public void recordGaugeDelta(double delta) {
        if (isEnabled()) {
            client.recordGaugeDelta(name, delta);
        }
    }

    /**
     * Records an occurrence of the specified named event.
     *
     * @param eventName the value to be added to the set
     */
    @Deprecated
    public void recordEvent(String eventName) {
    }

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * @param timeInMs the time in milliseconds
     */
    public void recordTimeInMillis(long timeInMs) {
        recordTimeInMillis(timeInMs, DEFAULT_SAMPLE_RATE);
    }

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * @param timeInMs   the time in milliseconds
     * @param sampleRate the sampling rate being employed. For example, a rate of 0.1 would tell StatsD that this timer is being sent sampled every 1/10th of the time,
     *                   so that it updates its timer_counters appropriately.
     */
    public void recordTimeInMillis(long timeInMs, double sampleRate) {
        if (isEnabled()) {
            client.recordExecutionTime(name, timeInMs, sampleRate);
        }
        recordTraceEvent("metric-timer", timeInMs, sampleRate);
    }

    /**
     * Records an execution time in milliseconds for the specified named operation. The execution time is calculated as the delta between the specified start
     * time and the current system time (using {@link System#currentTimeMillis()})
     *
     * @param systemTimeMillisAtStart the system time, in millis, at the start of the operation that has just completed
     */
    public void recordTimeInMillisToNow(long systemTimeMillisAtStart) {
        recordTimeInMillis(Math.max(0, System.currentTimeMillis() - systemTimeMillisAtStart));
    }
}
