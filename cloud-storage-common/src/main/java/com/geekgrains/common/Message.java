package com.geekgrains.common;

import java.io.Serializable;

public abstract class Message<T extends Message> implements Serializable {

    public T getMessage() {
        return (T) this;
    }

    public abstract Command getCommand();

}
