package junicorn.mrpc.demo.client.benchmark;

public interface ClientRunnable extends Runnable {

    RunnableStatistics getStatistics();
}