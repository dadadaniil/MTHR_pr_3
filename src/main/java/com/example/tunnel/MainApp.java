package com.example.tunnel;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Log4j2
public class MainApp {
    public static void main(String[] args) {
        Configurator.setRootLevel(Level.INFO);
        log.info("Starting tunnel simulation...");
        String fileName = "high_traffic_2_dir.txt";
        List<Callable<Void>> trains = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int maxTrains = Integer.parseInt(br.readLine().split("=")[1].trim());
            int maxDir = Integer.parseInt(br.readLine().split("=")[1].trim());


            TunnelController controller = TunnelController.getInstance(maxTrains, maxDir);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                int trainId   = Integer.parseInt(parts[0]);
                int direction = Integer.parseInt(parts[1]);
                long time     = Long.parseLong(parts[2]);
                trains.add(new Train(trainId, direction, time, controller));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            executor.invokeAll(trains);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }
}
