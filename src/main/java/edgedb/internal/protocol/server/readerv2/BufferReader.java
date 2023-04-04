package edgedb.internal.protocol.server.readerv2;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface BufferReader {
    public int read(ByteBuffer readInto);
}
