package org.vitrivr.cthulhu.scheduler;

public class MasterDispatcher extends Thread {
    MasterScheduler ms;
    public MasterDispatcher(MasterScheduler ms) {
        super();
        this.ms = ms;
    }
    @Override
    public void run() {
    }
}
