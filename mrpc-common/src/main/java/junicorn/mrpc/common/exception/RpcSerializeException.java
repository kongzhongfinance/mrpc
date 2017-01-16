package junicorn.mrpc.common.exception;

/**
 * Created by biezhi on 2016/11/6.
 */
public class RpcSerializeException extends MRpcException {

    public RpcSerializeException() {
    }

    public RpcSerializeException(String message) {
        super(message);
    }

    public RpcSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSerializeException(Throwable e) {
        super(e);
    }
}
