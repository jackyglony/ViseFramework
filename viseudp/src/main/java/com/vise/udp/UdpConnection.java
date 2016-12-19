package com.vise.udp;

import com.vise.log.ViseLog;
import com.vise.udp.common.UdpConfig;
import com.vise.udp.inter.IDataDispose;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-19 19:16
 */
public class UdpConnection {
    private InetSocketAddress connectedAddress;
    private DatagramChannel datagramChannel;
    private SelectionKey selectionKey;
    private int keepAliveMillis = UdpConfig.KEEP_ALIVE_MILLIS;
    private final ByteBuffer readBuffer, writeBuffer;
    private final IDataDispose dataDispose;
    private final Object writeLock = new Object();
    private long lastCommunicationTime;

    public UdpConnection (IDataDispose dataDispose, int bufferSize) {
        this.dataDispose = dataDispose;
        readBuffer = ByteBuffer.allocate(bufferSize);
        writeBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public void bind (Selector selector, InetSocketAddress localPort) throws IOException {
        close();
        readBuffer.clear();
        writeBuffer.clear();
        try {
            datagramChannel = selector.provider().openDatagramChannel();
            datagramChannel.socket().bind(localPort);
            datagramChannel.configureBlocking(false);//设置为非阻塞
            selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ);
            lastCommunicationTime = System.currentTimeMillis();
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public void connect (Selector selector, InetSocketAddress remoteAddress) throws IOException {
        close();
        readBuffer.clear();
        writeBuffer.clear();
        try {
            datagramChannel = selector.provider().openDatagramChannel();
            datagramChannel.socket().bind(null);
            datagramChannel.socket().connect(remoteAddress);
            datagramChannel.configureBlocking(false);//设置为非阻塞
            selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ);
            lastCommunicationTime = System.currentTimeMillis();
            connectedAddress = remoteAddress;
        } catch (IOException ex) {
            close();
            IOException ioEx = new IOException("Unable to connect to: " + remoteAddress);
            ioEx.initCause(ex);
            throw ioEx;
        }
    }

    public InetSocketAddress readFromAddress () throws IOException {
        DatagramChannel datagramChannel = this.datagramChannel;
        if (datagramChannel == null) throw new SocketException("Connection is closed.");
        lastCommunicationTime = System.currentTimeMillis();
        return (InetSocketAddress)datagramChannel.receive(readBuffer);
    }

    public Object readObject (UdpConnection connection) throws IOException {
        readBuffer.flip();
        try {
            try {
                Object object = dataDispose.read(connection, readBuffer);
                if (readBuffer.hasRemaining())
                    throw new IOException("Incorrect number of bytes (" + readBuffer.remaining()
                            + " remaining) used to deserialize object: " + object);
                return object;
            } catch (Exception ex) {
                throw new IOException("Error during deserialization.", ex);
            }
        } finally {
            readBuffer.clear();
        }
    }

    public int send (UdpConnection connection, Object object, SocketAddress address) throws IOException {
        DatagramChannel datagramChannel = this.datagramChannel;
        if (datagramChannel == null) throw new SocketException("Connection is closed.");
        synchronized (writeLock) {
            try {
                try {
                    dataDispose.write(connection, writeBuffer, object);
                } catch (Exception ex) {
                    throw new IOException("Error serializing object of type: " + object.getClass().getName(), ex);
                }
                writeBuffer.flip();
                int length = writeBuffer.limit();
                datagramChannel.send(writeBuffer, address);
                lastCommunicationTime = System.currentTimeMillis();
                boolean wasFullWrite = !writeBuffer.hasRemaining();
                return wasFullWrite ? length : -1;
            } finally {
                writeBuffer.clear();
            }
        }
    }

    public void close () {
        connectedAddress = null;
        try {
            if (datagramChannel != null) {
                datagramChannel.close();
                datagramChannel = null;
                if (selectionKey != null) selectionKey.selector().wakeup();
            }
        } catch (IOException ex) {
            ViseLog.e("Unable to close UDP connection." + ex);
        }
    }

    public boolean needsKeepAlive (long time) {
        return connectedAddress != null && keepAliveMillis > 0 && time - lastCommunicationTime > keepAliveMillis;
    }

}
