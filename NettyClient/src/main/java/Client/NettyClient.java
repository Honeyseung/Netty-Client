package Client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class NettyClient{
    private final String host;
    private final int port;

    private Channel serverChannel;
    private EventLoopGroup eventLoopGroup;

    public NettyClient(String host, int port){
        this.host=host;
        this.port=port;
    }

    public void connect() throws InterruptedException{
        eventLoopGroup = new NioEventLoopGroup(1,new DefaultThreadFactory("client"));
        Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup);

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(new InetSocketAddress(host,port));
        bootstrap.handler(new NettyClientInitializer());

        serverChannel = bootstrap.connect().sync().channel();
    }
    private void start() throws InterruptedException {
        Scanner scan = new Scanner(System.in);

        String msg;
        ChannelFuture future;

        while (true) {
            msg = scan.nextLine();

            future = serverChannel.writeAndFlush(msg.concat("\n"));

            if ("quit".equals(msg)) {
                serverChannel.closeFuture().sync();
                break;
            }
        }

        if (future != null) {
            future.sync();
        }
    }

    private void close() {
        eventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception{
        Config config = ConfigFactory.load();
        NettyClient client = new NettyClient("127.0.0.1", config.getInt("port"));
        try{
            client.connect();
            client.start();
        }finally{
            client.close();
        }
    }
}
