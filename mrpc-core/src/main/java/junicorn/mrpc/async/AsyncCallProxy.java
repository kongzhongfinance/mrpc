package junicorn.mrpc.async;

public interface AsyncCallProxy {

    RpcFuture call(String funcName, Object... args);

}