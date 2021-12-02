package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListDirectory extends Message<ListDirectory> {

    private final String currentDir;
    private final List<String> list;

    public ListDirectory(Path dir) throws Exception {
        currentDir = dir.toString();

        list = Files.walk(dir)
                .filter(Files::isDirectory)
                .filter(path -> path != dir)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

//    @Override
//    public ListDirectory getMessage() {
//        return this;
//    }

    @Override
    public Command getCommand() {
        return Command.LIST_DIRECTORIES;
    }
}
