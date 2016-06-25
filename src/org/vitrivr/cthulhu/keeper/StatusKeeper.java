package org.vitrivr.cthulhu.keeper;

import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

public interface StatusKeeper {
    public void saveStatus(CthulhuScheduler cs);
}
