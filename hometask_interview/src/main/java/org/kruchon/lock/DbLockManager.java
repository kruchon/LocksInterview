package org.kruchon.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbLockManager extends AStorageLockManager {

    private final JdbcTemplate jdbcTemplate;

    public static final String CREATE_LOCK_QUERY = "insert into db_locks values (?)";
    public static final String RELEASE_LOCK_QUERY = "delete from db_locks where id=?";

    @Autowired
    public DbLockManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected boolean tryToCreateLock(String id, long totalTimeToRetry) {
        try {
            jdbcTemplate.update(CREATE_LOCK_QUERY, id);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    protected void releaseLock(String id) {
        jdbcTemplate.update(RELEASE_LOCK_QUERY, id);
    }

}
