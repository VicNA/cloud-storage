package com.geekgrains.common;

import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class ListDirectories extends Message<ListDirectories> {

    private String clientRootDir;
    private String separator;
    private List<String> list;

    public ListDirectories() {
    }

    public ListDirectories(Path directory) throws Exception {
        buildList(directory);
    }

    public ListDirectories buildList(Path directory) throws IOException {
        clientRootDir = directory.toString();
        separator = directory.getFileSystem().getSeparator();

        list = Files.walk(directory)
                .filter(Files::isDirectory)
                .filter(path -> path != directory)
                .map(Path::toString)
                .collect(Collectors.toList());

        return this;
    }

    @Override
    public Command getCommand() {
        return Command.LIST_DIRECTORIES;
    }
}
