package junicorn.mrpc.common.exception;

/**
 * Created by biezhi on 2016/11/8.
 */
public class ServerInitalizserException extends MRpcException {

    public ServerInitalizserException() {
    }

    public ServerInitalizserException(String message) {
        super(message);
    }

    public ServerInitalizserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerInitalizserException(Throwable e) {
        super(e);
    }
}
