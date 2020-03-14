package kruchon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kruchon.lock.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.Callable;

import static java.util.concurrent.ForkJoinPool.commonPool;
import static kruchon.LockManagerTest.HistoryRecord.*;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        DbLockManager.class, InMemoryLockManager.class,
        SynchronizedInMemoryLockManager.class
})
public class LockManagerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbLockManager dbLockManager;

    @Autowired
    private InMemoryLockManager inMemoryLockManager;

    @Autowired
    private SynchronizedInMemoryLockManager synchronizedInMemoryLockManager;

    public enum HistoryRecord {
        T1_BEGIN, T2_BEGIN, T1_END, T2_END
    }

    private Collection<HistoryRecord> history = Collections.synchronizedList(new LinkedList<>());

    @Before
    public void setUp() {
        JdbcTemplateStub stub = new JdbcTemplateStub();
        when(jdbcTemplate.update(anyString(), anyString()))
                .then((invocationOnMock -> stub.update(invocationOnMock.getArgument(0),
                        invocationOnMock.getArgument(1).toString())));
    }

    @Test
    public void testDbLockManagerWorks() {
        testLockManagerWorks(dbLockManager);
    }

    @Test
    public void testInMemoryLockManagerWorks() {
        testLockManagerWorks(inMemoryLockManager);
    }

    @Test
    public void testSynchronizedInMemoryLockManagerWorks() {
        testLockManagerWorks(synchronizedInMemoryLockManager);
    }

    private void testLockManagerWorks(ALockManager lockManager) {
        Collection<Exception> exceptions = Collections.synchronizedSet(new HashSet<>());
        Callable<Object> firstTask = createFirstTask(lockManager, exceptions);
        Callable<Object> secondTask = createSecondTask(lockManager, exceptions);
        commonPool().invokeAll(List.of(firstTask, secondTask));
        assertTrue(exceptions.isEmpty());
        assertThat(history, containsInRelativeOrder(T1_BEGIN, T1_END, T2_BEGIN, T2_END));
    }

    private Callable<Object> createSecondTask(ALockManager lockManager, Collection<Exception> exceptions) {
        return () -> {
            try {
                Thread.sleep(100L);
                lockManager.doWithLock(() -> {
                    history.add(T2_BEGIN);
                    history.add(T2_END);
                    return null;
                }, "1");
            } catch (LockException | InterruptedException e) {
                exceptions.add(e);
            }
            return null;
        };
    }

    private Callable<Object> createFirstTask(ALockManager lockManager, Collection<Exception> exceptions) {
        return () -> {
            try {
                lockManager.doWithLock(() -> {
                    history.add(T1_BEGIN);
                    Thread.sleep(1000L);
                    history.add(T1_END);
                    return null;
                }, "1");
            } catch (LockException e) {
                exceptions.add(e);
            }
            return null;
        };
    }
}
