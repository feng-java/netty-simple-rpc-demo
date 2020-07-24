package com.study.netty.rpc.provider;

import com.study.netty.rpc.api.IRpcOperateService;

/**
 * TODO
 *
 * @author fengjiale
 * @date 2020-07-20
 */
public class RpcOperateService implements IRpcOperateService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public int mult(int a, int b) {
        return a * b;
    }

    @Override
    public int div(int a, int b) {
        return a / b;
    }
}
