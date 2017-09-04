package com.charles.network.nio;

/**
 * Created by Administrator on 2017/9/4.
 */
public class ISocketHandler {
    void send(ChangeRequest request, byte[] data);
    void send(ChangeRequest request);
}
