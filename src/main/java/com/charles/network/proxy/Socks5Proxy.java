package com.charles.network.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/8/17.
 * socks5 statue and response
 */
public class Socks5Proxy implements IProxy{
    public final static int ATYP_IP_V4 = 0x1;
    public final static int ATYP_DOMAIN_NAME = 0x3;
    public final static int ATYP_IP_V6 = 0x2;

    private Logger logger = Logger.getLogger(Socks5Proxy.class.getName());
    private enum STAGE {
        SOCK5_HELLO, SOCK5_ACK, SOCK5_READY
    }
    private STAGE _stage;

    public Socks5Proxy() {
        _stage = STAGE.SOCK5_HELLO;
    }

    @Override
    public TYPE getType() {
        return TYPE.SOCKS5;
    }

    @Override
    public boolean isReady() {
        return (_stage == STAGE.SOCK5_READY);
    }

    public byte[] getResponse(byte[] data) {
        byte[] respData = null;

        switch (_stage) {
            case SOCK5_HELLO:
                if (isMine(data)) {
                    respData = new byte[] {5, 0};
                } else {
                    respData = new byte[] {0, 91};
                }
                _stage = STAGE.SOCK5_ACK;
                break;
            case SOCK5_ACK:
                respData = new byte[] {5, 0, 0, 1, 0, 0, 0, 0, 0, 0};
                _stage = STAGE.SOCK5_READY;
            case SOCK5_READY:
                break;
        }
        return respData;
    }

    @Override
    public List<byte[]> getRemoteResponse(byte[] data) {
        List<byte[]> respData = null;
        int dataLength = data.length;

        /*
        to establish Sock5, there are two parties
        1. Hello(3 byts)
        2. ACK (3 bytes + dst info)
        as Client sending ACK, it might contain dst info.
        In this case, server needs to send back ACK response to client and start the remote socket right away,
        otherwise, client will wait until timeout.
         */
        if (_stage == STAGE.SOCK5_READY) {
            respData = new ArrayList<>(1);
            if (dataLength > 3) {
                //remove 3 bytes socks5 header
                dataLength -= 3;
                byte[] temp = new byte[dataLength];
                //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                System.arraycopy(data, 3, temp, 0, dataLength);
                respData.add(temp);
            }
        }
        return respData;
    }

    @Override
    public boolean isMine(byte[] data) {
        if (data[0] == 0x5) {
            return true;
        }
        return false;
    }
}
