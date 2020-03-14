package org.kruchon.lock;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class InMemoryLockManager extends AStorageLockManager {

    private final Set<String> locks = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected boolean tryToCreateLock(String id, long totalTimeToRetry) {
        return locks.add(id);
    }

    @Override
    protected void releaseLock(String id) {
        locks.remove(id);
    }
}
