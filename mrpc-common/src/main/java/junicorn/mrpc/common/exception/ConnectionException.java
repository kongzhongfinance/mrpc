package junicorn.mrpc.common.exception;

/**
 * Created by biezhi on 2017/1/16.
 */
public class ConnectionException extends MRpcException {

    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable e) {
        super(e);
    }

}
