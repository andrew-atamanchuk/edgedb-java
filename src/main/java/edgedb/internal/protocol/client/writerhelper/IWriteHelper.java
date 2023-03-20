package edgedb.internal.protocol.client.writerhelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public interface IWriteHelper {

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public void writeUUID(UUID uuid) throws IOException;
    public void writeUint8(int value) throws IOException;

    public void writeUint64(long value) throws IOException;
    public void writeUint32(int value) throws IOException;

    public void writeUint16(int value) throws IOException;

    public void writeString(String str) throws IOException;

    public void writeBytes(byte[] value) throws IOException;
}
