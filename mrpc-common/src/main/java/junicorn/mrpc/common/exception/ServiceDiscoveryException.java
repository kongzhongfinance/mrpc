package junicorn.mrpc.common.exception;

/**
 * Created by biezhi on 2016/11/8.
 */
public class ServiceDiscoveryException extends MRpcException {

    public ServiceDiscoveryException() {
    }

    public ServiceDiscoveryException(String message) {
        super(message);
    }

    public ServiceDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceDiscoveryException(Throwable e) {
        super(e);
    }
}
