package junicorn.mrpc.async;

import junicorn.mrpc.common.config.Constant;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * RpcFuture for async RPC call
 */
public class RpcFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFuture.class);

    private RpcRequest request;
    private RpcResponse response;
    private long startTime;

    private int retry = Constant.RPC_RETRY_COUNT;

    private CountDownLatch latch = new CountDownLatch(1);

    public RpcFuture(RpcRequest request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    public Object get() throws Throwable {
        latch.await();
        if (null != response) {
            if(null != response.getException()){
                throw response.getException();
            }
            return this.response.getResult();
        }
//        while(retry > 0){
//            latch.await(5, TimeUnit.SECONDS);
//            if (null != response) {
//                if(null != response.getException()){
//                    throw response.getException();
//                }
//                return this.response.getResult();
//            }
//            retry--;
//        }
//        long responseTime = System.currentTimeMillis() - startTime;
//        LOGGER.warn("Service response time is too slow. [{}] [{}.{}({})] . Response Time = {} ms", request.getRequestId(), request.getServiceName(), request.getMethodName(), Arrays.toString(request.getParameterTypes()), responseTime);
        return null;
    }

    public void done(RpcResponse reponse) {
        this.response = reponse;
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > Constant.RPC_RESPONSE_TIMEOUT) {
            LOGGER.warn("Service response time is too slow. [{}] [{}.{}({})] . Response Time = {} ms", request.getRequestId(), request.getServiceName(), request.getMethodName(), Arrays.toString(request.getParameterTypes()), responseTime);
        } else {
            LOGGER.debug("request [{}] [{}.{}({})] cost time : {}ms", request.getRequestId(), request.getServiceName(), request.getMethodName(), Arrays.toString(request.getParameterTypes()), responseTime);
        }
        latch.countDown();
    }

}