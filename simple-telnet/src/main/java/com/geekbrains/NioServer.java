package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    private String defaultPath = "";

    private final ByteBuffer buffer;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public NioServer() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.debug("Server started...");

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
//        channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
        if (sb.toString().trim().equals("ls")) {
            System.out.println(sb);
            sb.setLength(0);
            Path path = Paths.get(defaultPath);
            Files.walk(path, 1).forEach(p -> sb.append(p).append(System.lineSeparator()));
            channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));

        }

        if (sb.toString().trim().startsWith("cd ")) {
            String cur = sb.toString().trim().split(" ", 2)[1];
//            defaultPath = sb.toString().trim().split(" ", 2)[1];
            Path path;
            if (cur.equals("..")) {
                path = Paths.get(defaultPath).getParent();
                if (path == null) path = Paths.get("");
            } else {
                path = Paths.get(cur);
            }
            sb.setLength(0);
            sb.append(path).append(System.lineSeparator());
            channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
            defaultPath = path.toString();
        }
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
