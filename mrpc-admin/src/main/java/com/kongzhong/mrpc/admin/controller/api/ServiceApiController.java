package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.config.RpcAdminConst;
import com.kongzhong.mrpc.model.ServiceNodePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 服务API
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Slf4j
@RestController
@RequestMapping("api/service")
public class ServiceApiController {

    @GetMapping
    public Set<ServiceNodePayload> getNodeServices() {
        return RpcAdminConst.serviceNodePayloads;
    }

    @PostMapping
    public ResponseEntity<Integer> update(@RequestBody ServiceNodePayload serviceNodePayload) {

        log.info("Update: {}", serviceNodePayload);

        RpcAdminConst.serviceNodePayloads.add(serviceNodePayload);
        return ResponseEntity.ok(200);
    }

    /*@RequestMapping("invoke")
    public Object invoke(String address, int time) {
        CountDownLatch latch = new CountDownLatch(1);
        ServiceNodePayload serviceNodePayload = RpcAdminConst.serviceNodePayloads.stream().filter(node -> address.equals(node.getAddress())).findFirst().get();

        boolean isHttp = serviceNodePayload.getTransport().equals(TransportEnum.HTTP);

        final SimpleClientHandler[] simpleClientHandlers = {null};
        if (!clientHandlerMap.containsKey(address)) {
            NettyClient nettyClient = new NettyClient(new NettyConfig(), address);
            nettyClient.setTransport(serviceNodePayload.getTransport());
            nettyClient.createBootstrap(eventLoopGroup, (future) -> {
                if (future.isSuccess()) {
                    serviceNodePayload.setAvailAble(NodeAliveStateEnum.ALIVE);

                    Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
                    SimpleClientHandler handler = future.channel().pipeline().get(clientHandler);
                    simpleClientHandlers[0] = handler;
                    clientHandlerMap.put(address, handler);
                } else {
                    serviceNodePayload.setAvailAble(NodeAliveStateEnum.DEAD);
                }
                latch.countDown();
            });
        } else {
            simpleClientHandlers[0] = clientHandlerMap.get(address);
            latch.countDown();
        }

        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SimpleClientHandler simpleClientHandler = simpleClientHandlers[0];

        log.info("{}", simpleClientHandler);

        if (null != simpleClientHandler) {
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(StringUtils.getUUID());
            rpcRequest.setClassName(ClientConfig.class.getName());
            rpcRequest.setMethodName("setWaitTimeout");
            rpcRequest.setReturnType(Void.class);
            rpcRequest.setParameters(new Object[]{time});
            rpcRequest.setWaitTimeout(10_000);

            System.out.println(simpleClientHandler.getChannel());

            RpcCallbackFuture callbackFuture = simpleClientHandler.sendRequest(rpcRequest);
            try {
                Object o = callbackFuture.get();
                System.out.println("返回 : " + o);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }*/

}
