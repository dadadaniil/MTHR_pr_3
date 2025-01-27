package com.example.tunnel;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Log4j2
public class MainApp {
    private static final String FILE_NAME = "fast_n_slow_trains.txt";
    private static final int THREAD_POOL_SIZE = 5;

    public static void main(String[] args) {
        Configurator.setRootLevel(Level.INFO);
        log.info("Starting tunnel simulation...");

        List<Callable<Void>> trainTasks = new ArrayList<>();
        TunnelController controller;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            int maxTrains = parseConfigLine(br.readLine(), "maxTrainsInTunnel");
            int maxConsecutiveDirection = parseConfigLine(br.readLine(), "maxConsecutiveDirection");

            controller = TunnelController.getInstance(maxTrains, maxConsecutiveDirection);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 3) {
                    log.warn("Skipping invalid line: {}", line);
                    continue;
                }

                try {
                    int trainId = Integer.parseInt(parts[0].trim());
                    int direction = Integer.parseInt(parts[1].trim());
                    long travelTime = Long.parseLong(parts[2].trim());

                    trainTasks.add(new Train(trainId, direction, travelTime, controller));
                } catch (NumberFormatException e) {
                    log.error("Invalid data format in line: {}", line, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read input file: {}", FILE_NAME, e);
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<Future<Void>> results = executor.invokeAll(trainTasks);
            for (Future<Void> result : results) {
                result.get();
            }
            log.info("All trains have passed through the tunnel.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread execution interrupted", e);
        } catch (ExecutionException e) {
            log.error("Error occurred during task execution", e);
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

    private static int parseConfigLine(String line, String configKey) throws IOException {
        if (line == null || !line.startsWith(configKey + "=")) {
            throw new IOException("Invalid or missing config key: " + configKey);
        }
        return Integer.parseInt(line.split("=")[1].trim());
    }
}
