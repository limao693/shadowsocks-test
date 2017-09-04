package com.charles.network.nio;

/**
 * Created by Administrator on 2017/9/4.
 */
public class PipeEvent {
    public byte[] data;
    public boolean isEncrypted;
    
    public PipeEvent() {}

    public PipeEvent(byte[] data, boolean isEncrypted) {
        this.data = data;
        this.isEncrypted = isEncrypted;
    }
}
