package junicorn.mrpc.loadbalance;

import junicorn.mrpc.connection.Connection;

/**
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    Connection getConnection(Strategy strategy);

}
