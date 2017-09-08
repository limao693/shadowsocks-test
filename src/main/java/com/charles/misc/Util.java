package com.charles.misc;

import com.charles.network.proxy.Socks5Proxy;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            sb.append(String.format("%x", b & 0xff));
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

    public static String getRequestedHostInfo(byte[] data) {
        String ret = " ";
        int port;
        int neededLength;
        switch (data[0]) {
            case Socks5Proxy.ATYP_IP_V4:
                //IPV4
                neededLength = 6;
                if (data.length > neededLength) {
                    port = getPort(data[5], data[6]);
                    ret = String.format("%d.&d.%d.%d:%d", data[1], data[2], data[3], data[4], port);
                }
                break;
            case Socks5Proxy.ATYP_DOMAIN_NAME:
                //domain
                neededLength = data[1];
                if (data.length > neededLength + 3) {
                    port = getPort(data[neededLength + 2], data[neededLength + 3]);
                    String domain = bytesToString(data, 2, neededLength);
                    ret = String.format("%s:%d", domain, port);
                }
                break;
            case Socks5Proxy.ATYP_IP_V6:
                //IPV6
                //16 bytes of IP, 2 bytes of port
                neededLength = 18;
                if (data.length > neededLength) {
                    port = getPort(data[17], data[18]);
                    ret = String.format("%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%d",
                            data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8],
                            data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16],
                            port);
                }
                break;
        }
        return ret;
    }

    public static boolean saveFile(String fn, String content) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fn);
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public static byte[] composeSSHeader(String host, int port) {
        //TYPE (1 byte) + LENGTH (1 byte) + HOST(var byte) + PORT(2 bytes)
        byte[] respData = new byte[host.length() + 4];

        respData[0] = Socks5Proxy.ATYP_DOMAIN_NAME;
        respData[1] = (byte) host.length();
        System.arraycopy(host.getBytes(), 0, respData, 2, host.length());
        respData[host.length() + 2] = (byte) (port >> 8);
        respData[host.length() + 3] = (byte) (port & 0xFF);
        return respData;
    }

    public static String bytesToString(byte[] data, int start, int length) {
        String str = "";
        try {
            str = new String(data, start, length, "UTF-8");
        } catch (UnsupportedEncodingException  e) {
            e.printStackTrace();    //print at command line
        }
        return str;
    }

    public static String getFileContent(String fn) {
        Path path = Paths.get(fn);
        String content = "";
        try {
            content = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            // do nothing
        }

        return content;
    }

    private static short bytesToUnsignedByte(byte b) {
        return (short) (b & 0xff);
    }
    private static int getPort(byte b, byte b1) {
        return bytesToUnsignedByte(b) << 8 | bytesToUnsignedByte(b1);
    }
}
