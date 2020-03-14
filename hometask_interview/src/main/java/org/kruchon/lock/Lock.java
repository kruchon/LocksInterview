package org.kruchon.lock;

import java.io.Serializable;

public class Lock implements Serializable {
    private boolean isUsed = true;

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
