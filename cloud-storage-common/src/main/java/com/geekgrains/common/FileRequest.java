package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FileRequest extends Message<FileRequest> {

    private final String name;

    public FileRequest(String name) {
        this.name = name;
    }

    @Override
    public FileRequest getMessage() {
        return this;
    }

    @Override
    public Command getCommand() {
        return Command.FILE_REQUEST;
    }
}
