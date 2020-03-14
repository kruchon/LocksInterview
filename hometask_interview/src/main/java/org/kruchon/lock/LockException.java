package org.kruchon.lock;

public class LockException extends Exception {

    public LockException(String message) {
        super(message);
    }

    public LockException(Throwable cause) {
        super(cause);
    }

}
