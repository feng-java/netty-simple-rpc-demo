package com.study.netty.rpc.consumer.proxy;

import com.study.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODO
 *
 * @author fengjiale
 * @date 2020-07-21
 */
public class RpcProxy {
    public static <T> T create(Class<?> clazz){
        Class<?>[] interfaces = clazz.isInterface()?new Class<?>[]{clazz}:clazz.getInterfaces();
        T result = (T)Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,new MyInvocationHandle());
        return result;
    }



}

class MyInvocationHandle implements InvocationHandler{

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return rpcInvoke(method, args);
    }

    private Object rpcInvoke(Method method, Object[] args) {
        //封装传输协议
        InvokerProtocol invokerProtocol = new InvokerProtocol();
        invokerProtocol.setClassName(method.getDeclaringClass().getName());
        invokerProtocol.setMethodName(method.getName());
        invokerProtocol.setParams(method.getParameterTypes());
        invokerProtocol.setValues(args);
        RpcProxyHandle rpcProxyHandle = new RpcProxyHandle();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //自定义协议解码器
                        /** 入参有5个，分别解释如下
                         maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                         lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                         lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                         lengthAdjustment：要添加到长度字段值的补偿值
                         initialBytesToStrip：从解码帧中去除的第一个字节数
                         */
                        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        //自定义协议编码器
                        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                        //对象参数类型编码器
                        pipeline.addLast("encoder", new ObjectEncoder());
                        //对象参数类型解码器
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast("handler",rpcProxyHandle);
                    }
                });
        try {
            ChannelFuture channelFuture = b.connect("localhost", 8081).sync();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("future.channel().remoteAddress() = " + future.channel().remoteAddress());
                }
            });
            channelFuture.channel().writeAndFlush(invokerProtocol).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return rpcProxyHandle.getResponse();

    }
}
