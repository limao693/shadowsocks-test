package com.charles.network.nio;

import com.charles.Constant;
import com.charles.misc.Config;
import com.charles.misc.Util;
import com.charles.network.IServer;
import com.charles.ss.CryptFactory;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/9/4.
 */
public abstract class SocketHandlerBase implements IServer, ISocketHandler{
    private Logger logger = Logger.getLogger(SocketHandlerBase.class.getName());
    protected Selector _selector;
    protected Config _config;
    protected final List _pendingRequest = new LinkedList();
    protected final ConcurrentHashMap _pendingData = new ConcurrentHashMap();
    protected ConcurrentHashMap<SocketChannel, PipeWorker> _pipe = new ConcurrentHashMap<>();
    protected ByteBuffer _readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);

    protected abstract Selector initSelector() throws IOException;
    protected abstract boolean processPendingRequest(ChangeRequest request);
    protected abstract void processSelect(SelectionKey key);

    public SocketHandlerBase(Config config) throws IOException, InvalidAlgorithmParameterException {
        if (!CryptFactory.isCipherExisted(config.getMethod())) {
            throw new InvalidPropertiesFormatException(config.getMethod());
        }
        _config = config;
        _selector = initSelector();
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (_pendingRequest) {
                    Iterator changes = _pendingRequest.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        if (!processPendingRequest(change)) {
                            break;
                        }
                        changes.remove();
                    }
                }

                //wait events from selected channel
                _selector.select();

                Iterator selectedKeys = _selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    processSelect(key);
                }
            } catch (ClosedSelectorException e) {
                break;
            } catch (Exception e) {
                logger.warning(Util.getErrorMessage(e));
            }
        }
        logger.fine(this.getClass().getName() + " Closed.");
    }

    protected void creatWriterBuffer(SocketChannel socketChannel) {
        List queue = new ArrayList();
        Object put;
        put = _pendingData.putIfAbsent(socketChannel, queue);
        if (put != null) {
            logger.severe("Dup write buffer creation: " + socketChannel);
        }
    }

    protected void cheanUp(SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.info(Util.getErrorMessage(e));
        }
        SelectionKey key = socketChannel.keyFor(_selector);
        if (key != null) {
            key.cancel();
        }

        if (_pendingData.containsKey(socketChannel)) {
            _pendingData.remove(socketChannel);
        }
    }

    @Override
    public void send(ChangeRequest request, byte[] data) {
        switch (request.type) {
            case ChangeRequest.CHANGE_SOCKET_OP:
                List queue = (List) _pendingData.get(request.socket);
                if (queue != null) {
                    synchronized (queue) {
                        queue.add(ByteBuffer.wrap(data));
                    }
                } else  {
                    logger.warning(Util.getErrorMessage(new Throwable("Socket is closed: dropping this request")));

                }
                break;
        }

        synchronized (_pendingRequest) {
            _pendingRequest.add(request);
        }
        _selector.wakeup();
    }

    @Override
    public void send(ChangeRequest request) {
        send(request, null);
    }

    public void close() {
        for (PipeWorker P : _pipe.values()) {

        }
    }
}
