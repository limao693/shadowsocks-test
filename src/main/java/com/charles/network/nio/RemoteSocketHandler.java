package com.charles.network.nio;

import com.charles.misc.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.InvalidAlgorithmParameterException;
import java.util.List;
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

    public PipeWorker createPipe(ISocketHandler localHandler, SocketChannel localChannel, String ipAddress, int port) throws IOException {
        //prepare remote socket
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(ipAddress, port));

        //create write buffer for specified socket
        creatWriterBuffer(socketChannel);

        //create pipe worker for handing encrypt and decrypt
        PipeWorker pipe = new PipeWorker(localHandler, localChannel, this, socketChannel, _config);

        //setup pipe info
        //pipe.setRemoteChannel(socketChannel);
        _pipe.put(socketChannel, pipe);
        synchronized (_pendingRequest) {
            _pendingRequest.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER_ChANNEL, SelectionKey.OP_CONNECT));
        }
        return pipe;
    }

    @Override
    protected void processSelect(SelectionKey key) {
        try {
            if (key.isConnectable()) {
                finishConnection(key);
            }else if (key.isReadable()) {
                read(key);
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

    private void read(SelectionKey key) throws IOException{
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

    private void write(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List queue = (List) _pendingData.get(socketChannel);
        if (queue != null) {
            synchronized (queue) {
                //write data to socket
                while (!queue.isEmpty()) {
                    ByteBuffer buf = (ByteBuffer) queue.get(0);
                    socketChannel.write(buf);
                    if (buf.remaining() > 0) {
                        break;
                    }
                    queue.remove(0);
                }
                if (queue.isEmpty()) {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }else {
            logger.warning("RemoteSocket::Write queue = null: " + socketChannel.toString());
            return;
        }
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
