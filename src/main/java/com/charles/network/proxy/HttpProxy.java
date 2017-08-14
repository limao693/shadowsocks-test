package com.charles.network.proxy;

import com.charles.Constant;
import com.charles.misc.Util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/8/14.
 * local Http proxy statue and required
 */
public class HttpProxy implements IProxy{
    private static final String[] HTTP_METHODS =
            new String[] {"OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "CONNECT"};

    private Logger logger = Logger.getLogger(HttpProxy.class.getName());
    private boolean _isReady;
    private boolean _isHttpConnect;
    private Map<String, String> methodCache;

    public HttpProxy() {
        _isReady = false;
        _isHttpConnect = false;
    }

    public TYPE getType() {
        return TYPE.HTTP;
    }

    public boolean isReady() {
        return _isReady;
    }

    public byte[] getResponse(byte[] data) {
        if (methodCache == null) {
            methodCache = getHttpMethod(data);
        }
        setHttpMethod(methodCache);

        if (_isHttpConnect) {
            return String.format("HTTP/1.0 200\r\nProxy-agent:%s/%s\r\n\r\n",
                    Constant.PROG_NAME, Constant.VERSION).getBytes();
        }
        return null;
    }

    @Override
    public List<byte[]> getRemoteResponse(byte[] data) {
        //get host info, return ssHeader
        List<byte[]> respData = new ArrayList<>(2);
        String host;
        int port = 80;
        if (methodCache == null) {
            methodCache = getHttpMethod(data);
        }

        String[] hostInfo = methodCache.get("host").split(":");

        //get host name and port
        host = hostInfo[0];
        if (hostInfo.length > 1) {
            port = Integer.parseInt(hostInfo[1]);
        }

        byte[] ssHeader = Util.composeSSHeader(host, port);
        respData.add(ssHeader);
        if (!_isHttpConnect) {
            byte[] httpHeader = reconstructHttpHeader(methodCache, data);
            respData.add(httpHeader);
        }

        _isReady = true;
        return respData;
    }

    @Override
    public boolean isMine(byte[] data) {
        if (methodCache == null) {
            methodCache = getHttpMethod(data);
        }
        String method = methodCache.get("method");

        if (method != null) {
            for (String s : HTTP_METHODS) {
                if (s.equals(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> getHttpMethod(byte[] data) {
        String httpRequest = Util.bytesToString(data, 0, data.length);  //UTF-8 encoding
        String[] httpHeaders = httpRequest.split("\\r?\\n");    //回车 或者换行
        boolean isHostFound = true;

        /*正则：请求网络url为'http://',本地时'/','\s'为空白符
        GET /hello.txt HTTP/1.1
        User-Agent: curl/7.16.3 libcurl/7.16.3 OpenSSL/0.9.7l zlib/1.2.3
        Host: www.example.com
        Accept-Language: en, mi
         */
        Pattern pattern = Pattern.compile("^([a-zA-Z]*) [htps]{0,4}[:/]{0,3}(\\s[^/]*) (\\s*)");
        Map<String, String> header = new HashMap<>();
        if (httpHeaders.length > 0) {
            logger.fine("HTTP Header: " + httpHeaders[0]);
            Matcher matcher = pattern.matcher(httpHeaders[0]);
            if (matcher.find()) {   //find()部分匹配，matcher.matches()全部匹配
                header.put("method", matcher.group(1)); //如果该匹配的串有组还可以使用group(1)函数,匹配第一个（）匹配
                if (matcher.group(2).startsWith("/")) {
                    header.put("url", "/");
                    isHostFound = false;
                } else {
                    header.put("host", matcher.group(2));
                    header.put("url", matcher.group(3));
                }
                header.put("version", matcher.group(4));
            }
        }
        if (!isHostFound) {
            for (String line : httpHeaders) {
                if (line.toLowerCase().contains("host")) {
                    String info = line.split(":")[1].trim();    //将返回的放入info，使用trim()
                    header.put("host", info);
                    break;
                }
            }
        }
        return header;
    }

    private byte[] reconstructHttpHeader(Map<String, String> method, byte[] data) {
        String httpRequset = Util.bytesToString(data, 0, data.length);  //change to UTF-8
        String[] httpHeaders = httpRequset.split("\\r?\\n");    //end with \r or \n
        StringBuilder sb = new StringBuilder();
        boolean isFirstLine = true;
    }

    private void setHttpMethod(Map<String, String> header) {
        String method = header.get("method");

        if (method != null) {
            if (method.toUpperCase().equals("CONNECT")) {
                _isHttpConnect = true;
            } else {
                _isHttpConnect =false;
            }
        }
    }


}
