package com.example.tunnel;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class FileReaderUtil {
    private static final String FILE_NAME = "high_traffic_2_dir.txt";

    public static List<Train> readTrains(TunnelController controller) {
        List<Train> trainTasks = new ArrayList<>();

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
        }

        return trainTasks;
    }

    private static int parseConfigLine(String line, String configKey) throws IOException {
        if (line == null || !line.startsWith(configKey + "=")) {
            throw new IOException("Invalid or missing config key: " + configKey);
        }
        return Integer.parseInt(line.split("=")[1].trim());
    }
}