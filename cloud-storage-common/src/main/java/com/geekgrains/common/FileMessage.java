package com.geekgrains.common;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FileMessage implements Message {

    private static final int BUTCH_SIZE = 8192;

    private final String name;
    private final long size;
    private final byte[] bytes;
    private final boolean isFirstButch;
    private final int endByteNum;
    private final boolean isFinishBatch;

    public FileMessage(String name,
                       long size,
                       byte[] bytes,
                       boolean isFirstButch,
                       int endByteNum,
                       boolean isFinishBatch) {
        this.name = name;
        this.size = size;
        this.bytes = bytes;
        this.isFirstButch = isFirstButch;
        this.endByteNum = endByteNum;
        this.isFinishBatch = isFinishBatch;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}