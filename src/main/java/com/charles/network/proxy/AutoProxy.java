package com.charles.network.proxy;

import com.charles.misc.Reflection;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/8/11.
 * proxy class for Sock5 and Http
 */
public class AutoProxy implements IProxy{
    private Logger logger = Logger.getLogger(AutoProxy.class.getName());
    private IProxy _proxy;
    private volatile boolean isInitialized;

    public AutoProxy() {
        isInitialized = false;
    }

    @Override
    public boolean isReady() {
        return (isInitialized && _proxy.isReady());
    }

    @Override
    public TYPE getType() {
        return TYPE.AUTO;
    }

    @Override
    public byte[] getResponse(byte[] data) {
        if (!isInitialized) {
            init(data);
        }
        return _proxy.getResponse(data);
    }

    @Override
    public List<byte[]> getRemoteResponse(byte[] data) {
        if (!isInitialized) {
            init(data);
        }
        return _proxy.getRemoteResponse(data);
    }

    @Override
    public boolean isMine(byte[] data) {
        if (!isInitialized) {
            init(data);
        }
        return _proxy.isMine(data);
    }

    private void init(byte[] data) {
        Object obj;
        IProxy proxy;
        for (Map.Entry<IProxy.TYPE, String > entry : ProxyFactory.proxies.entrySet()) {
            if (entry.getKey() == this.getType()) {
                continue;
            }

            obj = Reflection.get(entry.getValue());
            proxy = (IProxy) obj;
            if (proxy.isMine(data)) {
                logger.fine("ProxyType(Auto): " + proxy.getType());
                _proxy = proxy;
                isInitialized =true;
                return;
            }
        }
        logger.severe("Unable to determine proxy type.");
    }
}
