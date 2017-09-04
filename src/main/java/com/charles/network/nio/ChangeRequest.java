package com.charles.network.nio;

import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2017/9/4.
 * Request for nio socket hander
 */
public class ChangeRequest {
    public static final int REGISTER_ChANNEL = 1;
    public static final int CHANGE_SOCKET_OP = 2;
    public static final int CLOSE_CHANNEL = 3;

    public SocketChannel socket;
    public int type;
    public int op;

    public ChangeRequest(SocketChannel socket, int type, int op) {
        this.socket = socket;
        this.type = type;
        this.op = op;
    }

    public ChangeRequest(SocketChannel socket, int type) {
        this(socket, type, 0);
    }
}
