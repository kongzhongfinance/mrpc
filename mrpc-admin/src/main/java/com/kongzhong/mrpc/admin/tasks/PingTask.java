package com.kongzhong.mrpc.admin.tasks;

import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.task.annotation.Schedule;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.enums.NodeStatusEnum;
import com.kongzhong.mrpc.utils.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author biezhi
 * @date 2018/6/8
 */
@Bean
@Slf4j
public class PingTask {

    private Set<String> addresses = new HashSet<>();

    private static final int DEFAULT_TIME_OUT = 5000;

    @Inject
    private ServerService serverService;

    public void initUrl(Set<String> addresses) {
        log.info("init urls: {}", addresses);
        this.addresses = addresses;
    }

    public void addUrl(String address) {
        this.addresses.add(address);
    }

    @Schedule(name = "pingServer", cron = "*/30 * * * * ?", delay = 10_000L)
    public void pingServer() {
        log.info("ping server");
        addresses.parallelStream().forEach(this::accept);
    }

    private void accept(String address) {
        try {
            String  url  = "http://" + address + "/status";
            String  host = address.split(":")[0];
            Integer port = Integer.valueOf(address.split(":")[1]);

            if (!pingHost(host, port, DEFAULT_TIME_OUT)) {
                serverService.updateStatus(host, port, NodeStatusEnum.OFFLINE);
            } else {
                int code = HttpRequest.get(url).connectTimeout(DEFAULT_TIME_OUT).readTimeout(DEFAULT_TIME_OUT).code();
                if (code == 200) {
                    serverService.updateStatus(host, port, NodeStatusEnum.ONLINE);
                } else {
                    serverService.updateStatus(host, port, NodeStatusEnum.OFFLINE);
                }
            }
        } catch (Exception e) {
            log.error("执行出错了", e);
        }
    }

    public boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

}
