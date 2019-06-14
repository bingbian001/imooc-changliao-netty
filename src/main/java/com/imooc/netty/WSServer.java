package com.imooc.netty;

import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * webserver服务
 * @author liuzhihao
 */
@Component
public class WSServer {
	
	//单例模式
	private static class SingletionWSServer {
		static final WSServer instance = new WSServer();
	}
	
	public static WSServer getInstance(){
		return SingletionWSServer.instance;
	}
	
	private EventLoopGroup mainGroup;
	private EventLoopGroup subGroup;
	private ServerBootstrap server;
	private ChannelFuture channelFuture;
	
	public WSServer() {
		// 定义一对线程组
		mainGroup = new NioEventLoopGroup();
		subGroup = new NioEventLoopGroup();
		// 创建netty 服务器， ServerBootstrap 是一个启动类
		server = new ServerBootstrap();
		server.group(mainGroup, subGroup) 				// 设置主从线程
			.channel(NioServerSocketChannel.class)  // 设置 nio的双向通道
			.childHandler(new WSServiceInitializer());  // 子处理器， 用于处理 subGroup
	}
	
	public void start(){
		// 启动server类, 并且设置端口为8088
		this.channelFuture = server.bind(8088);
		System.err.println("netty websocket server 启动完毕...");
	}
}
