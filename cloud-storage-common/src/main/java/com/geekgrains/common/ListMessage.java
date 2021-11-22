package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListMessage extends Message {

    private final List<String> files;

    public ListMessage(Path dir) throws Exception {
        setType(CommandType.LIST_MESSAGE);
        files = Files.list(dir).map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }
}
