package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class NioServer {

    private static final int PORT = 8189;
    private static final int BUFFER_SIZE = 10;
    private static final String DEFAULT_PATH = System.getenv("USERPROFILE");

    private final ByteBuffer buffer;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    private String currentPath;

    public NioServer() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.debug("Server started...");

            currentPath = DEFAULT_PATH;
            serverHandle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serverHandle() throws IOException {
        while (selector.select() > 0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) doAccept();
                if (key.isReadable()) doRead(key);
                iterator.remove();
            }
        }
    }

    private void doRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        while (channel.isOpen()) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
            }
            if (read == 0) {
                break;
            }

            if (read > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    sb.append((char) buffer.get());
                }
                buffer.clear();
            }
        }

        log.debug("Received: {}", sb);

        if (sb.toString().trim().equals("ls")) {
            sb.setLength(0);
            Path path = Paths.get(currentPath);
            Files.walk(path, 1).forEach(p -> sb.append(p.getFileName()).append(System.lineSeparator()));
            channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
        }

        if (sb.toString().trim().startsWith("cd ")) {
            Path path = getPath(sb.toString());
            if (path != null) {
                sb.setLength(0);
                sb.append(path.toAbsolutePath()).append(System.lineSeparator());
                channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
                currentPath = path.toAbsolutePath().toString();
            }
        }

        if (sb.toString().trim().startsWith("cat ")) {
            Path path = getPath(sb.toString());
            sb.setLength(0);
            if (Files.isDirectory(path)) {
                sb.append("cat: ").append(path.getFileName()).append(": Is a directory");
            } else {
                ByteBuffer buf = ByteBuffer.allocate(10);
                byte[] result;
                try (SeekableByteChannel byteChannel = Files.newByteChannel(path)) {
                    result = new byte[(int) byteChannel.size()];
                    int pos = 0;
                    while (byteChannel.read(buf) > 0) {
                        buf.flip();
                        while (buf.hasRemaining()) {
                            result[pos++] = buf.get();
                        }
                        buf.clear();
                    }
                }
                sb.append(new String(result, StandardCharsets.UTF_8)).append(System.lineSeparator());
            }
            channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
        }

        if (sb.toString().trim().startsWith("mkdir ")) {
            String dir = sb.toString().trim().split(" ", 2)[1];
            if (!dir.equals(currentPath)) {
                if (!(dir.contains(currentPath)  || dir.contains("\\") || dir.contains("/"))) {
                    Path path = Paths.get(currentPath).resolve(dir);
                    if (!Files.exists(path)) {
                        Files.createDirectory(path);
                        sb.setLength(0);
                        sb.append("Directory ").append(dir).append(" created").append(System.lineSeparator());
                        channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
        }

        if (sb.toString().trim().startsWith("touch ")) {
            String file = sb.toString().trim().split(" ", 2)[1];
            if (!file.equals(currentPath)) {
                if (!(file.contains(currentPath)  || file.contains("\\") || file.contains("/"))) {
                    Path path = Paths.get(currentPath).resolve(file);
                    if (!Files.exists(path)) {
                        Files.createFile(path);
                        sb.setLength(0);
                        sb.append("File ").append(file).append(" created").append(System.lineSeparator());
                        channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
        }

        channel.write(ByteBuffer.wrap(String.format("telnet %s>", currentPath).getBytes(StandardCharsets.UTF_8)));
    }

    private Path getPath(String pathString) {
        String cur = pathString.trim().split(" ", 2)[1];
        Path path = null;
        if (cur.equals("..")) {
            path = Paths.get(currentPath).getParent();
            if (path == null) path = Paths.get(currentPath);
        } else {
            if (!cur.equals(currentPath)) {
                if (cur.contains(currentPath) || cur.contains("\\") || cur.contains("/")) {
                    path = Paths.get(cur);
                } else {
                    path = Paths.get(currentPath, cur);
                }
            }
        }
        return path;
    }

    private void doAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        log.debug("Client connected...");
        String str = "Welcome message for user" + System.lineSeparator();
        channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
    }
}
