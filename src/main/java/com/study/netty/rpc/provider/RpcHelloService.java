package com.study.netty.rpc.provider;

import com.study.netty.rpc.api.IRpcHelloService;

/**
 *
 *
 * @author fengjiale
 * @date 2020-07-20
 */
public class RpcHelloService implements IRpcHelloService {

    @Override
    public String hello(String name) {
        System.out.println("provider的hello方法收到了name = " + name);
        return "hello," + name;
    }
}
