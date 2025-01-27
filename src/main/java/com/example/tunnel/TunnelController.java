package com.example.tunnel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TunnelController {

    private static volatile TunnelController instance;

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
            synchronized (TunnelController.class) {
                if (instance == null) {
                    instance = new TunnelController(maxTrains, maxDir);
                }
            }
        }
        return instance;
    }

    public void enterTunnel(int trainId, int requestedDirection) throws InterruptedException {
        lock.lock();
        try {
            while (!canTrainEnter(requestedDirection)) {
                canEnter.await();
            }

            directionInUse = (directionInUse == -1) ? requestedDirection : directionInUse;
            currentInTunnel++;
            if (requestedDirection == directionInUse) {
                consecutiveCount++;
            } else {

                directionInUse = requestedDirection;
                consecutiveCount = 1;
            }
            log.info("Train " + trainId + " entering. Direction="
                + requestedDirection + ", inTunnel=" + currentInTunnel);
        } finally {
            lock.unlock();
        }
    }

    public void exitTunnel(int trainId) {
        lock.lock();
        try {
            currentInTunnel--;
            log.info("Train " + trainId + " exiting. inTunnel=" + currentInTunnel);
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
        if (directionInUse == -1) {

            return currentInTunnel < maxTrainsInTunnel;
        }
        if (directionInUse == requestedDirection) {

            if (currentInTunnel < maxTrainsInTunnel
                && consecutiveCount < maxConsecutiveDirection) {
                return true;
            }
        } else {

            if (currentInTunnel == 0) {

                return true;
            }
        }
        return false;
    }
}
