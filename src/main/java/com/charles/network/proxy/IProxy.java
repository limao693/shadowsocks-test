package com.charles.network.proxy;

import java.util.List;

/**
 * Created by Administrator on 2017/7/15.
 */
public interface IProxy {
    enum TYPE {
        SOCKS5, HTTP, AUTO
    }

    boolean isReady();
    TYPE getType();
    byte[] getResponse(byte[] data);
    List<byte[]> getRemoteResponse(byte[] data);
    boolean isMine(byte[] data);
}
