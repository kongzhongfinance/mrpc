package junicorn.mrpc.common.exception;

/**
 * Created by biezhi on 2016/11/20.
 */
public class ServerHandlerException extends MRpcException {

    public ServerHandlerException() {
    }

    public ServerHandlerException(String message) {
        super(message);
    }

    public ServerHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerHandlerException(Throwable e) {
        super(e);
    }
}
