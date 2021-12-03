package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListFiles extends Message<ListFiles> {

    private final String path;
    private List<String> list;

    public ListFiles(String path) {
        this.path = path;
    }

    public ListFiles buildList() throws Exception {
        list = Files.list(Paths.get(path))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());

        return this;
    }

    @Override
    public ListFiles getMessage() {
        return this;
    }

    @Override
    public Command getCommand() {
        return Command.LIST_FILES;
    }
}
