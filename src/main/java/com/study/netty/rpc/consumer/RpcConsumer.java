package com.study.netty.rpc.consumer;

import com.study.netty.rpc.api.IRpcHelloService;
import com.study.netty.rpc.consumer.proxy.RpcProxy;

/**
 * TODO
 *
 * @author fengjiale
 * @date 2020-07-21
 */
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService iRpcHelloService = RpcProxy.create(IRpcHelloService.class);
        String result = iRpcHelloService.hello("家乐");
        System.out.println("result = " + result);
    }

}
