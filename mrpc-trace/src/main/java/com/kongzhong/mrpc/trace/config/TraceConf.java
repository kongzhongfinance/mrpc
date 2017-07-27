package com.kongzhong.mrpc.trace.config;

import java.util.List;

/**
 * TraceConf
 */
public class TraceConf {

    /**
     * Enable the trace or not
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * The trace server
     */
    private String server;

    /**
     * The app name
     */
    private String name;

    /**
     * The app owner
     */
    private String owner;

    /**
     * The url patterns
     */
    private List<TracePoint> points;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<TracePoint> getPoints() {
        return points;
    }

    public void setPoints(List<TracePoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "TraceConf{" +
                "enable=" + enable +
                ", server='" + server + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", points=" + points +
                '}';
    }
}
