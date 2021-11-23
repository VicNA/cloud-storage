package com.geekbrains;

import com.geekgrains.common.FileMessage;
import com.geekgrains.common.FileRequest;
import com.geekgrains.common.ListMessage;
import com.geekgrains.common.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    private Path serverRootDir;
    private byte[] buffer;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverRootDir = Paths.get("cloud-storage-server", "cloud");
        ctx.writeAndFlush(new ListMessage(serverRootDir));
        buffer = new byte[8192];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        switch (msg.getType()) {
            case FILE_MESSAGE:
                processFile((FileMessage) msg, ctx);
                break;
            case FILE_REQUEST:
                sendFile((FileRequest) msg, ctx);
                break;
        }
    }

    private void sendFile(FileRequest msg, ChannelHandlerContext ctx) throws IOException {
        boolean isFirstButch = true;
        Path filePath = serverRootDir.resolve(msg.getName());
        long size = Files.size(filePath);
        try (FileInputStream is = new FileInputStream(serverRootDir.resolve(msg.getName()).toFile())){
            int read;
            while ((read = is.read(buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(buffer)
                        .name(filePath.getFileName().toString())
                        .size(size)
                        .isFirstButch(isFirstButch)
                        .isFinishBatch(is.available() <= 0)
                        .endByteNum(read)
                        .build();
                ctx.writeAndFlush(message);
                isFirstButch = false;
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
    }

    private void processFile(FileMessage msg, ChannelHandlerContext ctx) throws Exception {
        Path file = serverRootDir.resolve(msg.getName());
        if (msg.isFirstButch()) {
            Files.deleteIfExists(file);
        }

        try(FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(msg.getBytes(), 0, msg.getEndByteNum());
        }

        if (msg.isFinishBatch()) {
            ctx.writeAndFlush(new ListMessage(serverRootDir));
        }
    }
}
