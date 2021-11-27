package com.geekgrains.common;

import java.io.Serializable;

public abstract class Message<T extends Message> implements Serializable {

    public abstract T getMessage();

    public abstract Command getCommand();

}
