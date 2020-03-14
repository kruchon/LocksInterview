package org.kruchon.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Aspect
@Component
public class LockAspect {

    private final DbLockManager dbLockManager;
    private final InMemoryLockManager inMemoryLockManager;
    private final SynchronizedInMemoryLockManager synchronizedInMemoryLockManager;

    @Autowired
    public LockAspect(DbLockManager dbLockManager, InMemoryLockManager inMemoryLockManager, SynchronizedInMemoryLockManager synchronizedInMemoryLockManager) {
        this.dbLockManager = dbLockManager;
        this.inMemoryLockManager = inMemoryLockManager;
        this.synchronizedInMemoryLockManager = synchronizedInMemoryLockManager;
    }

    @Around("@annotation(org.kruchon.annotations.DbLock) && args(id,..)")
    public Object dbLock(ProceedingJoinPoint joinPoint, String id) throws Throwable {
        return dbLockManager.doWithLock(wrapToCallable(joinPoint), id);
    }

    @Around("@annotation(org.kruchon.annotations.InMemoryLock) && args(id,..)")
    public Object inMemoryLock(ProceedingJoinPoint joinPoint, String id) throws Throwable {
        return inMemoryLockManager.doWithLock(wrapToCallable(joinPoint), id);
    }

    @Around("@annotation(org.kruchon.annotations.SynchronizedInMemoryLock) && args(id,..)")
    public Object synchronizedInMemoryLock(ProceedingJoinPoint joinPoint, String id) throws Throwable {
        return synchronizedInMemoryLockManager.doWithLock(wrapToCallable(joinPoint), id);
    }

    private Callable<Object> wrapToCallable(ProceedingJoinPoint joinPoint) {
        return ()-> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new LockException(throwable);
            }
        };
    }
 }
