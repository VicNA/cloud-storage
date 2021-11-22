package com.geekgrains.common;

import java.io.Serializable;

public class Message implements Serializable {

    private CommandType type;

    protected void setType(CommandType type) {
        this.type = type;
    }

    public CommandType getType() {
        return type;
    }
}
