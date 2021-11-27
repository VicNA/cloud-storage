package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListFile extends Message<ListFile> {

    private final List<String> list;

    public ListFile(Path dir) throws Exception {
        list = Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public ListFile getMessage() {
        return this;
    }

    @Override
    public Command getCommand() {
        return Command.LIST_FILE;
    }
}
