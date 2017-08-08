package com.charles.ss;

import com.charles.misc.Util;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/8/7.
 * Shadowsocks key generator
 * 使用SecretKey接口
 */
public class ShadowSocksKey implements SecretKey{
    private Logger logger = Logger.getLogger(ShadowSocksKey.class.getName());
    //key is length is 32
    private final static int KEY_LENGTH = 32;
    private byte[] _key;
    private int _length;

    public ShadowSocksKey(String password) {
        _length = KEY_LENGTH;
        //init： String to byte[]
        _key = init(password);
    }

    public ShadowSocksKey(String password, int length) {
        _length = length;
        _key = init(password);
    }

    private byte[] init(String password) {
        //MessDig: 使用其中的MD5加密（16位）
        MessageDigest md = null;
        byte[] keys = new byte[KEY_LENGTH];
        byte[] temp = null;
        byte[] hash = null;
        byte[] passwordBytes = null;
        int i = 0;

        try {
            md = MessageDigest.getInstance("MD5");
            passwordBytes = password.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.info("ShadowSocksKey: Unsupported string encoding");
        }
        catch (Exception e) {
            //TODO getErrorMessage 含义
            logger.info(Util.getErrorMessage(e));
            return null;
        }

        while (i < keys.length) {
            if (i == 0) {
                hash = md.digest(passwordBytes);
                //temp 长度为byte长度与hash长度和
                temp = new byte[passwordBytes.length + hash.length];
            } else {
                //数组拷贝，MD5加密
                System.arraycopy(hash, 0, temp, 0, hash.length);
                System.arraycopy(passwordBytes, 0, temp, hash.length, passwordBytes.length);
                hash = md.digest(temp);
            }
            System.arraycopy(hash, 0, keys, i, hash.length);
            i += hash.length;
        }

        if (_length != KEY_LENGTH) {
            byte[] keysl = new byte[_length];
            System.arraycopy(keys, 0, keysl, 0, _length);
            return keysl;
        }
        return keys;
    }

    @Override
    public String getAlgorithm() {
        return "shadowsocks";
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        return _key;
    }

}
