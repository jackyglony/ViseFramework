package com.vise.udp.inter;

import com.vise.udp.UdpConnection;

import java.nio.ByteBuffer;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-19 19:35
 */
public interface IDataDispose {
    IDataDispose DEFAULT = new IDataDispose() {
        @Override
        public void write(UdpConnection connection, ByteBuffer buffer, Object object) {

        }

        @Override
        public Object read(UdpConnection connection, ByteBuffer buffer) {
            return null;
        }

        @Override
        public int getLengthLength() {
            return 0;
        }

        @Override
        public void writeLength(ByteBuffer buffer, int length) {

        }

        @Override
        public int readLength(ByteBuffer buffer) {
            return 0;
        }
    };

    void write(UdpConnection connection, ByteBuffer buffer, Object object);

    Object read(UdpConnection connection, ByteBuffer buffer);

    int getLengthLength();

    void writeLength(ByteBuffer buffer, int length);

    int readLength(ByteBuffer buffer);
}
