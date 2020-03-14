package org.kruchon.lock;

import java.util.concurrent.Callable;

public abstract class ALockManager {
    protected static final long DEFAULT_TOTAL_TIME_TO_RETRY_MS = 30000L;

    public <T> T doWithLock(Callable<T> callable, String id) throws LockException {
        return doWithLock(callable, id, DEFAULT_TOTAL_TIME_TO_RETRY_MS);
    }

    public <T> T doWithLock(Callable<T> callable, String id, long totalTimeToRetry) throws LockException {
        createLock(id, totalTimeToRetry);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            releaseLock(id);
        }
    }

    protected abstract void createLock(String id, long totalTimeToRetry) throws LockException;

    protected abstract void releaseLock(String id);

}
