package com.burning8393.herostory;

import com.burning8393.herostory.cmdhandler.CmdHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameServer.class);

    public static void main(String[] args) {
        CmdHandlerFactory.init();
        GameMsgRecongnizer.init();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class); // 服务器信道的处理方式
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // 客户端信道的处理器方式
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                        new HttpServerCodec(), // Http 服务器编解码器
                        new HttpObjectAggregator(65535), // 内容长度限制
                        new WebSocketServerProtocolHandler("/websocket"), // Websocket协议处理器，在这里处理握手、ping、pong等消息
                        new GameMsgDecoder(), // 自定义的消息编码器
                        new GameMsgEncoder(), // 自定义的消息编码器
                        new GameMsgHandler() // 自定义的消息编码器
                    );

                }
            });
            // 绑定12345端口
            // 注意：实际项目中会使用 argArray 中的参数来指定端口号
            ChannelFuture f = bootstrap.bind(12345).sync();
            if (f.isSuccess()) {
                LOGGER.info("服务器启动成功");
            }

            // 等待服务器信道关闭，
            // 也就是不要退出应用程序
            // 让应用程序可以一直提供服务
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
