package com.charles.network.nio;

import com.charles.misc.Config;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.InvalidAlgorithmParameterException;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/9/4.
 * Handler for processing all IO event for remote sockets
 */
public class RemoteSocketHandler extends SocketHandlerBase{
    private Logger logger = Logger.getLogger(RemoteSocketHandler.class.getName());

    public RemoteSocketHandler(Config config) throws IOException, InvalidAlgorithmParameterException {
        super(config);
    }

    @Override
    protected Selector initSelector() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    @Override
    protected boolean processPendingRequest(ChangeRequest request) {
        if ((request.type != ChangeRequest.REGISTER_ChANNEL) && request.socket.isConnected()) {
            return false;
        }

        SelectionKey key;
        switch (request.type) {
            case ChangeRequest.CHANGE_SOCKET_OP:
                key = request.socket.keyFor(_selector);
                if ((key != null) && key.isValid()) {
                    key.interestOps(request.op);
                }else {
                    logger.warning("RemoteSocketHandler::processPendingRequest (drop): " + key + request.socket);

                }
                break;
            case ChangeRequest.REGISTER_ChANNEL:
                try {
                    request.socket.register(_selector, request.op);
                }catch (ClosedChannelException e) {
                    //socket get closed by remote
                    logger.warning(e.toString());
                    cleanUp(request.socket);
                }
                break;
            case ChangeRequest.CLOSE_CHANNEL:
                cleanUp(request.socket);
                break;

        }
        return true;
    }

    @Override
    protected void processSelect(SelectionKey key) {
        try {
            if (key.isConnectable()) {
                finishConnection(key);
            }else if (key.isReadable()) {
                ready(key);
            }else if (key.isWritable()) {
                write(key);
            }
        }catch (IOException e) {
            cleanUp((SocketChannel) key.channel());
        }
    }

    private void finishConnection(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            logger.warning("RemoteSockethandler::finishConnection I/O exception: "+ e.toString());
            cleanUp(socketChannel);
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void ready(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PipeWorker pipe = _pipe.get(socketChannel);
        if (pipe != null) {
            //should not happen
            cleanUp(socketChannel);
            return;
        }

        //clear read buffer for new data
        _readBuffer.clear();

        //read data
        int readCount;
        try {
            readCount = socketChannel.read(_readBuffer);
        }catch (IOException e) {
            //remote socket closed
            cleanUp(socketChannel);
            return;
        }

        if (readCount == -1) {
            cleanUp(socketChannel);
            return;
        }
        
        //Handle the response
        pipe.processData(_readBuffer.array(), readCount, false);
    }

    @Override
    protected void cleanUp(SocketChannel socketChannel) {
        super.cheanUp(socketChannel);

        PipeWorker pipe = _pipe.get(socketChannel);
        if (pipe != null) {
            pipe.close();
            _pipe.remove(socketChannel);
            logger.info("RemoteSocket closed: " + pipe.socketInfo);
        }else {
            logger.info("RemoteSocket closed(Null): " + socketChannel);
        }
    }
}
