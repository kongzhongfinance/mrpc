package junicorn.mrpc.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import junicorn.mrpc.common.exception.MRpcException;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.model.RpcResponse;
import junicorn.mrpc.inteceptor.InterceptorChain;
import junicorn.mrpc.inteceptor.RpcInteceptor;
import junicorn.mrpc.inteceptor.RpcInvocation;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> handlerMap;

    private InterceptorChain interceptorChain = new InterceptorChain();

    public RpcServerHandler(Map<String, Object> handlerMap, List<RpcInteceptor> inteceptors) {
        this.handlerMap = handlerMap;
        if(null != inteceptors && !inteceptors.isEmpty()){
            int pos = inteceptors.size();
            for(RpcInteceptor rpcInteceptor : inteceptors){
                interceptorChain.addLast("server_interceptor_" + (pos--), rpcInteceptor);
            }
        }
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx,final RpcRequest request) throws Exception {
        RpcServer.submit(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("Receive request " + request.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getRequestId());
                try {
                    Object result = handle(request);
                    response.setResult(result);
                } catch (Exception e) {
                    if(e instanceof InvocationTargetException){
                        InvocationTargetException invocationTargetException = (InvocationTargetException) e;
                        Throwable t = invocationTargetException.getTargetException();
                        MRpcException mRpcException = new MRpcException(t);
                        LOGGER.error("RPC Server handle request error", t);
                        response.setException(mRpcException);
                    } else {
                        LOGGER.error("RPC Server handle request error", e);
                        response.setException(e);
                    }
                } catch (Throwable t) {
                    response.setException(new MRpcException(t.getCause()));
                } finally {
                    ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            LOGGER.debug("Send response for request " + request.getRequestId());
                        }
                    });
                }
            }
        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getServiceName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

        List<RpcInteceptor> interceptors = interceptorChain.getInterceptors();
        if(null != interceptors && !interceptors.isEmpty()){
            //执行拦截器链
            RpcInvocation invocation = new RpcInvocation(serviceBean, serviceFastMethod, parameterTypes, parameters, interceptors);
            return invocation.next();
        } else{
            return serviceFastMethod.invoke(serviceBean, parameters);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        LOGGER.error("server caught exception", cause);
    }
}
