package junicorn.mrpc.loadbalance;

import junicorn.mrpc.connection.ConnManager;
import junicorn.mrpc.connection.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by biezhi on 2016/12/30.
 */
public class SampleLoadBalance implements LoadBalance {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleLoadBalance.class);

    private AtomicInteger posInt = new AtomicInteger(0);
    private Random random = new Random();

    @Override
    public Connection getConnection(Strategy strategy) {
        try {
            while(ConnManager.connections.isEmpty()){
                TimeUnit.SECONDS.sleep(1);
            }
            List<Connection> connections = new ArrayList<>(ConnManager.getConns());
            if(connections.size() == 1){
                return connections.get(0);
            }
            if(strategy == Strategy.POLL){
                return this.poll(connections);
            }
            if(strategy == Strategy.RANDOM){
                return this.random(connections);
            }
            if(strategy == Strategy.LAST){
                return this.last(connections);
            }
        } catch (Exception e){
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     * poll load connection
     *
     * @param connections
     * @return
     */
    private Connection poll(List<Connection> connections){
        try {
            int pos = posInt.get();
            if(pos >= connections.size()){
                posInt.set(0);
                pos = posInt.get();
            }
            Connection connection = connections.get(pos);
            posInt.addAndGet(1);
            return connection;
        } catch (Exception e){
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     * random load connection
     *
     * @param connections
     * @return
     */
    private Connection random(List<Connection> connections){
        try {
            int randomPos = 0;
            int max = connections.size();
            if(max != 1){
                randomPos = random.nextInt(max);
            }
            return connections.get(randomPos);
        } catch (Exception e){
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     * last load connection
     *
     * @param connections
     * @return
     */
    private Connection last(List<Connection> connections){
        return connections.get(connections.size() - 1);
    }


}
