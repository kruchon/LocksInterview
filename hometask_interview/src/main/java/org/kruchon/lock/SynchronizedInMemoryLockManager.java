package org.kruchon.lock;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SynchronizedInMemoryLockManager extends ALockManager {

    private static final long RETRY_AFTER_TIME_MS = 100L;

    private static final long ENTRIES_NUM_IN_CACHE = 1000L;
    private Cache<String, Lock> cache;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void buildCache() {
        CacheManager cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder().build();
        cacheManager.init();

        CacheConfigurationBuilder<String, Lock> builder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        String.class, Lock.class,
                        ResourcePoolsBuilder.heap(ENTRIES_NUM_IN_CACHE))
                .withEvictionAdvisor((lockId, lock)-> !lock.isUsed());
        cache = cacheManager.createCache("synchronizedCache", builder);
    }

    @Override
    protected void createLock(String id, long totalTimeToRetry) throws LockException {
        Lock oldLock = cache.putIfAbsent(id, new Lock());
        if (oldLock != null) {
            synchronized (oldLock) {
                waitForReleaseIfUsed(oldLock, totalTimeToRetry);
                oldLock.setUsed(true);
            }
        }
    }

    private void waitForReleaseIfUsed(Lock oldLock, long totalTimeToRetry) throws LockException {
        long startTime = System.currentTimeMillis();
        while (oldLock.isUsed()) {
            try {
                oldLock.wait(RETRY_AFTER_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long endTime = System.currentTimeMillis();
            if (endTime - startTime >= totalTimeToRetry) {
                throw new LockException("Total time to retry was exceed");
            }
        }
    }

    @Override
    protected void releaseLock(String id) {
        Lock lock = cache.get(id);
        synchronized (lock) {
            lock.setUsed(false);
            lock.notify();
        }
    }

}
