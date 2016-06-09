package org.vitrivr.cthulhu.scheduler;

import java.util.Properties;

public class SchedulerFactory {
    public SchedulerFactory() {
    }
    public CthulhuScheduler createScheduler(String type, Properties props) {
        if(type.equals("worker")) {
            return new WorkerScheduler(props);
        }
        if(type.equals("coordinator")) {
            return new CoordinatorScheduler(props);
        }
        return null;
    }
}
