package com.kongzhong.mrpc.trace;

/**
 * TraceConstants
 */
public interface TraceConstants {

    /**
     * Trace id
     */
    String TRACE_ID = "traceId";

    /**
     * Span id
     */
    String SPAN_ID = "spanId";

    /**
     * The cs annotation
     */
    String ANNO_CS = "cs";

    /**
     * The cr annotation
     */
    String ANNO_CR = "cr";

    /**
     * The sr annotation
     */
    String ANNO_SR = "sr";

    /**
     * The ss annotation
     */
    String ANNO_SS = "ss";

    /**
     * The server receive time
     */
    String SR_TIME = "srt";

    /**
     * The server send time
     */
    String SS_TIME = "sst";
}
