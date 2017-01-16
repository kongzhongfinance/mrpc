package junicorn.mrpc.common.exception;

public class MRpcException extends RuntimeException {

    public MRpcException() {
    }

    public MRpcException(String message) {
        super(message);
    }

    public MRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public MRpcException(Throwable e) {
        super(e);
    }

}