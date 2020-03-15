package org.kruchon.lock;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class InMemoryLockManager extends AStorageLockManager {

    private final Set<String> locks = new ConcurrentSkipListSet<>();

    @Override
    protected boolean tryToCreateLock(String id, long totalTimeToRetry) {
        return locks.add(id);
    }

    @Override
    protected void releaseLock(String id) {
        locks.remove(id);
    }
}
