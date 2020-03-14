package org.kruchon.lock;

public abstract class AStorageLockManager extends ALockManager {
    private static final long RETRY_AFTER_TIME_MS = 10L;

    @Override
    protected void createLock(String id, long totalTimeToRetry) throws LockException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < totalTimeToRetry) {
            boolean isLockCreated = tryToCreateLock(id, totalTimeToRetry);
            if (isLockCreated) {
                return;
            } else {
                doDelay();
            }
        }
        throw new LockException("Total time was exceed");
    }

    protected abstract boolean tryToCreateLock(String id, long totalTimeToRetry);

    private void doDelay() {
        try {
            Thread.sleep(RETRY_AFTER_TIME_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
