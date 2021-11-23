package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListMessage implements Message {

    private final List<String> files;

    public ListMessage(Path dir) throws Exception {
        files = Files.list(dir).map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_MESSAGE;
    }
}
