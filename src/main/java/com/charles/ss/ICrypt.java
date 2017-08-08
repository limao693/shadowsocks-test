package com.charles.ss;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2017/7/15.
 */
public interface ICrypt {
    void encrypt(byte[] data, ByteArrayOutputStream stream);
    void encrypt(byte[] data, int length, ByteArrayOutputStream stream);
    void decrypt(byte[] data, ByteArrayOutputStream stream);
    void decrypt(byte[] data, int length, ByteArrayOutputStream stream);
    int getIVLength();
    int getKeyLength();

}
