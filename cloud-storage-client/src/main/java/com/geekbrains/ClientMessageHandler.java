package com.geekbrains;

import com.geekgrains.common.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientMessageHandler  extends SimpleChannelInboundHandler<Message> {

    private Callback callback;

    public ClientMessageHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        log.debug("Received: {}", msg);
        callback.call(msg);
    }
}
