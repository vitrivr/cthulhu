package org.vitrivr.cthulhu.keeper;

import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

public interface StatusKeeper {
    /**
     * Serializes the status of the CthulhuScheduler object. It saves Job table, Worker table, and
     * runtime properties to persistent storage.
     */
    public void saveStatus(CthulhuScheduler cs);
}
