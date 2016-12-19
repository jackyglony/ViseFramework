package com.vise.udp.inter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-19 19:41
 */
public interface ServerDiscoveryHandler {
    ServerDiscoveryHandler DEFAULT = new ServerDiscoveryHandler() {
        private ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

        @Override
        public boolean onDiscoverHost(DatagramChannel datagramChannel, InetSocketAddress fromAddress, IDataDispose
                dataDispose) throws IOException {
            datagramChannel.send(emptyBuffer, fromAddress);
            return true;
        }
    };

    boolean onDiscoverHost(DatagramChannel datagramChannel, InetSocketAddress fromAddress, IDataDispose dataDispose)
            throws IOException;
}
