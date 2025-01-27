package com.example.tunnel;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Train implements Callable<Void> {
    private final int trainId;
    private final int direction;
    private final TunnelController controller;
    private final long travelTime;

    public Train(int trainId, int direction, long travelTime, TunnelController controller) {
        this.trainId = trainId;
        this.direction = direction;
        this.travelTime = travelTime;
        this.controller = controller;
    }

    @Override
    public Void call() throws InterruptedException {
        controller.enterTunnel(trainId, direction);
        log.info("Train {} is traveling through tunnel...", trainId);
        TimeUnit.SECONDS.sleep(travelTime);
        controller.exitTunnel(trainId);
        return null;
    }
}
