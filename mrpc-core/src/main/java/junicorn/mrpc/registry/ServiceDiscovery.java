package junicorn.mrpc.registry;

/**
 * 服务发现
 */
public interface ServiceDiscovery {

    String discover();

    void stop();
}
