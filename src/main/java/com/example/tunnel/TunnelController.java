package com.example.tunnel;

import java.util.concurrent.locks.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TunnelController {

    private static volatile TunnelController instance;
    private static final Lock initLock = new ReentrantLock();

    private final Lock lock = new ReentrantLock();
    private final Condition canEnter = lock.newCondition();

    private final int maxTrainsInTunnel;
    private final int maxConsecutiveDirection;

    private int currentInTunnel = 0;
    private int directionInUse = -1;
    private int consecutiveCount = 0;

    private TunnelController(int maxTrainsInTunnel, int maxConsecutiveDirection) {
        this.maxTrainsInTunnel = maxTrainsInTunnel;
        this.maxConsecutiveDirection = maxConsecutiveDirection;
    }

    public static TunnelController getInstance(int maxTrains, int maxDir) {
        if (instance == null) {
            initLock.lock();
            try {
                if (instance == null) {
                    instance = new TunnelController(maxTrains, maxDir);
                    log.info("TunnelController initialized with maxTrains={}, maxConsecutiveDirection={}",
                        maxTrains, maxDir);
                }
            } finally {
                initLock.unlock();
            }
        }
        return instance;
    }

    public static TunnelController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TunnelController has not been initialized. Call getInstance(int, int) first.");
        }
        return instance;
    }

    public void enterTunnel(int trainId, int requestedDirection) throws InterruptedException {
        lock.lock();
        try {
            while (!canTrainEnter(requestedDirection)) {
                canEnter.await();
            }

            if (directionInUse == -1) {
                directionInUse = requestedDirection;
            }

            currentInTunnel++;
            consecutiveCount = (requestedDirection == directionInUse) ? consecutiveCount + 1 : 1;
            log.info("Train {} entering. Direction={}, inTunnel={}", trainId, requestedDirection, currentInTunnel);
        } finally {
            lock.unlock();
        }
    }

    public void exitTunnel(int trainId) {
        lock.lock();
        try {
            currentInTunnel--;
            log.info("Train {} exiting. inTunnel={}", trainId, currentInTunnel);
            if (currentInTunnel == 0) {
                directionInUse = -1;
                consecutiveCount = 0;
            }
            canEnter.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean canTrainEnter(int requestedDirection) {
        return (directionInUse == -1 || requestedDirection == directionInUse && consecutiveCount < maxConsecutiveDirection)
            && currentInTunnel < maxTrainsInTunnel;
    }
}
