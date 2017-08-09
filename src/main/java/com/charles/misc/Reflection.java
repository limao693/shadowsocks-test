package com.charles.misc;

import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2017/8/9.
 */
public class Reflection {
    public static Object get(String className, Object... args) {
        Object retValue = null;
        try {
            Class c = Class.forName(className);
            if (args.length == 0) {
                retValue = c.newInstance();
            }
            else if ((args.length & 1) == 0) {
                Class[] oParm = new Class[args.length / 2];
                for (int arg_i = 0, i = 0; arg_i < args.length; arg_i += 2, i++) {
                    oParm[i] = (Class) args[arg_i];
                }

                Constructor constructor = c.getConstructor(oParm);
                Object[] paramObjs = new Object[args.length / 2];
                for (int arg_i = 1, i = 0; arg_i < args.length; arg_i+=2, i++) {
                    paramObjs[i] = args[arg_i];
                }
                retValue = constructor.newInstance(paramObjs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retValue;
    }
}
