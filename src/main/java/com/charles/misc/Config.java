package com.charles.misc;

//import com.sun.jmx.remote.internal.IIOPProxy;
import com.charles.network.proxy.IProxy;
import com.charles.ss.AesCrypt;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by Administrator on 2017/7/14.
 * Config data to server
 */
public class Config {
    private String _ipAddr;
    private int _port;
    private String _localIpAddr;
    private int _localPort;
    private String _method;
    private String _password;
    private String _logLevel;
    private IProxy.TYPE _proxyType;

    public Config() {
        loadFromJson("");
    }

    public Config(String ipAddr, int port, String localIpAddr, int localPort, String method, String password) {
        //loadFromJson at first;
        this();
        _ipAddr = ipAddr;
        _port = port;
        _localIpAddr = localIpAddr;
        _localPort = localPort;
        _method = method;
        _password = password;
        //Auto config IproxyType
        _proxyType = IProxy.TYPE.AUTO;

    }

    public Config(String ipAddr, int port, String localIpAddr, int localPort, String method, String password, IProxy.TYPE type) {
        this(ipAddr, port, localIpAddr, localPort, method, password);
        _proxyType = type;
    }

    //
    public void setRemoteIpAddress(String value) {
        _ipAddr = value;
    }

    public String getRemoteIpAddress() {
        return _ipAddr;
    }

    public void setLocalIpAddress(String value) {
        _localIpAddr = value;
    }

    public String getLocalIpAddress() {
        return _localIpAddr;
    }

    public void setRemotePort(int value) {
        _port = value;
    }

    public int getRemotePort() {
        return _port;
    }

    public void setLocalPort(int value) {
        _localPort = value;
    }

    public int getLocalPort() {
        return _localPort;
    }


    public void set_proxyType(String value) {
        _proxyType = IProxy.TYPE.AUTO;
        //Eliminate the effect of letter case on the results
        if (value.toLowerCase().equals(IProxy.TYPE.HTTP.toString().toLowerCase())) {
            _proxyType = IProxy.TYPE.HTTP;
        } else if (value.toLowerCase().equals(IProxy.TYPE.SOCKS5.toString().toLowerCase())){
            _proxyType = IProxy.TYPE.SOCKS5;
        }
    }

    public void set_proxyType (IProxy.TYPE value) {
        _proxyType = value;
    }

    public IProxy.TYPE get_proxyType (){
        return _proxyType;
    }

    public void set_logLevel(String value) {
        _logLevel = value;
        //Avoid conflict, extract after initialization
        //TODO LOG
        Log.init(get_LogLevel());
    }

    public String get_LogLevel() {
        return _logLevel;
    }

    public void loadFromJson(String jsonStr) {
        if (jsonStr.length() == 0) {
            //config as {}
            jsonStr = "{}";
        }

        JSONObject jObj = (JSONObject) JSONValue.parse(jsonStr);
        _ipAddr = (String) jObj.getOrDefault("remoteIpAddress", "");
        _port = ((Number) jObj.getOrDefault("remotePort", 1080)).intValue();
        _localIpAddr = (String) jObj.getOrDefault("localIpAddress", "127.0.0.1");
        _localPort = ((Number) jObj.getOrDefault("localPort", 1080)).intValue();
        //TODO AesCrypt 加密模块
        _method = (String) jObj.getOrDefault("method", AesCrypt.CIPHER_AES_256_CFB);
        _password = (String) jObj.getOrDefault("password", "");
        _logLevel = (String ) jObj.getOrDefault("logLevel", "INFO");
        set_proxyType((String) jObj.getOrDefault("proxyType", IProxy.TYPE.SOCKS5.toString().toLowerCase()));
        set_logLevel(_logLevel);
    }

    public String saveToJson() {
        JSONObject jObj = new JSONObject();
        jObj.put("remoteIpAddress", _ipAddr);
        jObj.put("remotePort", _port);
        jObj.put("localIpAddress", _localIpAddr);
        jObj.put("localPort", _localPort);
        jObj.put("method", _method);
        jObj.put("password", _password);
        jObj.put("proxyType", _proxyType.toString().toLowerCase());
        jObj.put("logLevel", _logLevel);

        //TODO Util
        return Util.prettyPrintJson(jObj);
    }

}
