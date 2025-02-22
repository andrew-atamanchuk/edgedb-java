package edgedb.internal.buffer;

import java.nio.ByteBuffer;

import static edgedb.internal.protocol.constants.CommonConstants.BUFFER_SIZE;

public class SingletonBuffer {

    private volatile static SingletonBuffer uniqueBuffer;
    private ByteBuffer buffer;

    public SingletonBuffer() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public static SingletonBuffer getInstance() {

        if (uniqueBuffer == null) {
            uniqueBuffer = new SingletonBuffer();
        }

        return uniqueBuffer;
    }

    public ByteBuffer getBuffer() {
        buffer.clear();
        return buffer;
    }
}
