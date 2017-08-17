package com.charles.ss;

import com.charles.misc.Reflection;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/8/8.
 * Crypt factory
 */
public class CryptFactory {
    private static final Map<String, String> crypts = new HashMap<String, String>(){{
        putAll(AesCrypt.getCiphers());
        putAll(CamelliaCrypt.getCiphers());
        putAll(BlowFishCrypt.getCiphers());
        putAll(SeedCrypt.getCiphers());
    }};

    private static Logger logger = Logger.getLogger(CryptFactory.class.getName());

    public static boolean isCipherExisted(String name) {
        return (crypts.get(name)) != null;
    }

    public static ICrypt get(String name, String password) {
        try {
            //反射机制，查询密码类型
            Object obj = Reflection.get(crypts.get(name), String.class, name, String.class, password);
            return (ICrypt) obj;
        } catch (Exception e) {
            //日志打印由Util单元完成
            logger.info(com.charles.misc.Util.getErrorMessage(e));
        }
        return null;
    }

    //Used in the main function, return List
    public static List<String> getSupportedCiphers() {
        List sortedKeys = new ArrayList<>(crypts.keySet());
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

}
