package com.example.tunnel;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Log4j2
public class MainApp {
    private static final int THREAD_POOL_SIZE = 5;

    public static void main(String[] args) {
        Configurator.setRootLevel(Level.INFO);
        log.info("Starting tunnel simulation...");

        TunnelController controller = null;
        List<Train> trainTasks = FileReaderUtil.readTrains(controller);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<Callable<Void>> callables = trainTasks.stream()
                .map(task -> (Callable<Void>) () -> {
                    task.run();
                    return null;
                })
                .collect(Collectors.toList());

            try {
                List<Future<Void>> results = executor.invokeAll(callables);
                for (Future<Void> result : results) {
                    result.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread execution interrupted", e);
            } catch (ExecutionException e) {
                log.error("Error occurred during task execution", e);
            }
            log.info("All trains have passed through the tunnel.");
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("Forcing executor shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}