package com.geekgrains.common;

import java.io.Serializable;

public interface Message extends Serializable {

    CommandType getType();

//    private CommandType type;

//    protected void setType(CommandType type) {
//        this.type = type;
//    }
//
//    public CommandType getType() {
//        return type;
//    }
}
