package kruchon;

import org.springframework.dao.DataAccessException;

import static org.kruchon.lock.DbLockManager.CREATE_LOCK_QUERY;

public class JdbcTemplateStub {

    private static final String ALREADY_LOCKED = "Already locked";
    private boolean isLocked = false;

    public synchronized int update(String sql, Object... args) {
        if (CREATE_LOCK_QUERY.equals(sql)) {
            if (isLocked) {
                throw new DataAccessException(ALREADY_LOCKED) {
                };
            }
            isLocked = true;
        } else {
            isLocked = false;
        }
        return 0;
    }

}
