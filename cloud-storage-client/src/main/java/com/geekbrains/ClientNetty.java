package com.geekbrains;

import com.geekgrains.common.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientNetty {

    private SocketChannel channel;

    public ClientNetty(String host, int port) {

        Thread thread = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                channel = socketChannel;
                                channel.pipeline().addLast(
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder()//,
//                                    new ClientMessageHandler()
                                );
                            }
                        });

                ChannelFuture future = bootstrap.connect(host, port).sync();

                log.debug("Client started...");
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("e", e);
            } finally {
                worker.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void close() {
        channel.close();
    }

    public void sendMessage(Message msg) {
        channel.writeAndFlush(msg);
    }
}
