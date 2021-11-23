package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FileRequest implements Message {

    private final String name;

    public FileRequest(String name) {
        this.name = name;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
