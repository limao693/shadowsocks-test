package com.charles.network.nio;

import com.charles.Constant;
import com.charles.misc.Config;
import com.charles.misc.Util;
import com.charles.network.proxy.IProxy;
import com.charles.network.proxy.ProxyFactory;
import com.charles.ss.CryptFactory;
import com.charles.ss.ICrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/9/4.
 */
public class PipeWorker implements Runnable {
    private Logger logger = Logger.getLogger(PipeWorker.class.getName());
    private SocketChannel _localChannel;
    private SocketChannel _remoteChannel;
    private ISocketHandler _localSocketHandler;
    private ISocketHandler _remoteSocketHandler;
    private IProxy _proxy;
    private ICrypt _crypt;
    public String socketInfo;
    private ByteArrayOutputStream _outStream;
    private BlockingQueue _processQueue;
    private volatile boolean requestedClose;

    public PipeWorker(ISocketHandler localHandler, SocketChannel localChannel, ISocketHandler remoteHandler, SocketChannel remoteChannel, Config config) {
        _localChannel = localChannel;
        _remoteChannel = remoteChannel;
        _localSocketHandler = localHandler;
        _remoteSocketHandler = remoteHandler;
        _crypt = CryptFactory.get(config.getMethod(), config.getPassword());
        _proxy = ProxyFactory.get(config.get_proxyType());
        _outStream = new ByteArrayOutputStream(Constant.BUFFER_SIZE);
        _processQueue = new LinkedBlockingQueue();
        requestedClose = false;
        socketInfo = String.format("Local: %s, Remote: %s", localChannel, remoteChannel);
    }

    public void close(){
        requestedClose = true;
        processData(null, 0, false);
    }

    public void processData(byte[] data, int count, boolean isEncrypted) {
        if (data != null) {
            byte[] dataCopy = new byte[count];
            System.arraycopy(data, 0, dataCopy, 0, count);
            _processQueue.add(new PipeEvent(dataCopy, isEncrypted));
        } else {
            _processQueue.add(new PipeEvent());
        }
    }

    public void forceClose() {
        logger.fine("PipwWorker:: forceClose " + socketInfo);

        //close socket now
        try {
            if (_localChannel.isOpen()) {
                _localChannel.close();
            }
            if (_remoteChannel.isOpen()) {
                _remoteChannel.close();
            }
        } catch (IOException e) {
            logger.fine("PipeWorker::forceClose> " + e.toString());
        }
        //shandard close
        close();
    }

    @Override
    public void run() {
        PipeEvent event;
        ISocketHandler socketHandler;
        SocketChannel channel;
        List<byte[]> sendData = null;

        while (true) {
            //make sure all the request in the queue is empty
            if (_processQueue.isEmpty() && requestedClose) {
                logger.fine("PipeWorker closed (" + _processQueue.size() + ")" + socketInfo);
                if (_localChannel.isOpen()) {
                    _localSocketHandler.send(new ChangeRequest(_localChannel, ChangeRequest.CLOSE_CHANNEL));
                }
                if (_remoteChannel.isOpen()) {
                    _remoteSocketHandler.send(new ChangeRequest(_remoteChannel, ChangeRequest.CLOSE_CHANNEL));
                }
                break;
            }
            try {
                event = (PipeEvent) _processQueue.take();

                //if event data is null, it means that it's a wake up call
                //to check if any other thread is requested to close sockets
                if (event.data == null) {
                    continue;
                }
                //process proxy packet if needed
                if (!_proxy.isReady()) {
                    // packet for local socket
                    byte[] temp = _proxy.getResponse(event.data);
                    if (temp != null) {
                        _localSocketHandler.send(new ChangeRequest(_localChannel, ChangeRequest.CHANGE_SOCKET_OP,
                                SelectionKey.OP_WRITE), temp);
                    }
                    //packet for remote socket
                    sendData = _proxy.getRemoteResponse(event.data);
                    if (sendData != null) {
                        continue;
                    }
                    //index 0 is always ss payload
                    logger.info("Connected to: " + Util.getRequestedHostInfo(sendData.get(0)));
                    //logger.info("");
                }else {
                    sendData.clear();
                    sendData.add(event.data);
                }

                for (byte[] bytes : sendData) {
                    //empty for new data
                    _outStream.reset();

                    if (event.isEncrypted) {
                        _crypt.encrypt(bytes, _outStream);
                        channel = _remoteChannel;
                        socketHandler = _remoteSocketHandler;
                    }else {
                        _crypt.decrypt(bytes, _outStream);
                        channel = _localChannel;
                        socketHandler = _localSocketHandler;
                    }

                    //data is ready to send to socket
                    ChangeRequest request = new ChangeRequest(channel, ChangeRequest.CHANGE_SOCKET_OP, SelectionKey.OP_WRITE);
                    socketHandler.send(request, _outStream.toByteArray());
                }
            } catch (InterruptedException e) {
                logger.fine(Util.getErrorMessage(e));
                break;
            }
        }
    }
}
