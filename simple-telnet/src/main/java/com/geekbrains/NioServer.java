package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioServer {

    private static final int PORT = 8189;
    private static final int BUFFER_SIZE = 10;

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
//        while (serverChannel.isOpen()) {
        while (selector.select() > 0) {
//            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
//            for (SelectionKey key : keys) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) doAccept();
                if (key.isReadable()) doRead(key);
                iterator.remove();
            }
//            keys.clear();
        }
    }

    private StringBuilder doRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder("From server: ");
        while (channel.isOpen()) {
            int read = channel.read(buffer);
            log.debug("Input byte: {}", read);
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
        channel.write(ByteBuffer.wrap(sb.toString().trim().getBytes(StandardCharsets.UTF_8)));
    }

    private void doAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        log.debug("Client connected...");
        channel.write(ByteBuffer.wrap("Welcome message for user".getBytes(StandardCharsets.UTF_8)));
    }
}
