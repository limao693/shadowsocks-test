package com.charles.misc;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.SecureRandom;
import java.sql.Statement;

/**
 * Created by Administrator on 2017/7/15.
 */
public class Util {
    //TODO 有何作用？稍后解释
    public static String dumpBytes(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length*2);
        for(byte b: a)
            sb.append(String.format("%", b & 0xff));
        return sb.toString();
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static String getErrorMessage(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter pWriter = new PrintWriter(writer);
        e.printStackTrace(pWriter);
        return writer.toString();
    }

    public static String prettyPrintJson(final JSONObject jObj) {
        //print Json to String, implement at Config.java
        String retValue;
        StringWriter writer = new StringWriter() {
            private final static String indent = " ";
            private final String LINE_SEP = System.getProperty("line.separator");
            private int indentLevel = 0;

            @Override
            public void write(int c) {
                char ch = (char) c;
                if (ch == '[' || ch == '{') {
                    super.write(c);
                    super.write(LINE_SEP);
                    indentLevel++;
                    writeIndentation();
                } else if (ch == ']' || ch == '}') {
                    super.write(LINE_SEP);
                    indentLevel--;
                    writeIndentation();
                    super.write(c);
                } else if (ch == ':') {
                    super.write(c);
                    super.write(" ");
                } else if (ch == ',') {
                    super.write(c);
                    super.write(LINE_SEP);
                    writeIndentation();
                } else {
                    super.write(c);
                }
            }

            private void writeIndentation() {
                for (int i = 0; i < indentLevel; i++)
                    super.write(indent);
            }

        };

        try {
            jObj.writeJSONString(writer);
            retValue = writer.toString();
        } catch (IOException e) {
            //如果存储错误，使用默认方式存取
            retValue = jObj.toJSONString();
        }

        return retValue;
    }
}
