package org.vitrivr.cthulhu.runners;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class CoordinatorRunner extends CthulhuRunner {
    public void setupCoordinator(int delay) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                }
            }, delay, delay, TimeUnit.SECONDS);
    }
    public void setupWorker() {
    }
}
