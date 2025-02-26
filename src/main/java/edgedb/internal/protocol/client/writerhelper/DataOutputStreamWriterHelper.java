package edgedb.internal.protocol.client.writerhelper;

import lombok.AllArgsConstructor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class DataOutputStreamWriterHelper implements IWriteHelper {
    DataOutputStream dataOutputStream;


    @Override
    public void writeUUID(UUID uuid) throws IOException {
        if(uuid != null) {
            byte[] value = IWriteHelper.asBytes(uuid);
            dataOutputStream.write(value, 0, value.length);
        }
    }

    @Override
    public void writeUint8(int value) throws IOException {
        dataOutputStream.writeByte(value);
    }

    @Override
    public void writeUint64(long value) throws IOException {
        dataOutputStream.writeLong(value);
    }

    @Override
    public void writeUint32(int value) throws IOException {
        dataOutputStream.writeInt(value);
    }

    @Override
    public void writeUint16(int value) throws IOException {
        dataOutputStream.writeShort(value);
    }

    @Override
    public void writeString(String str) throws IOException {
        if (str != null) {
            dataOutputStream.writeInt(str.getBytes().length);
            dataOutputStream.write(str.getBytes(), 0, str.getBytes().length);
        }
    }

    @Override
    public void writeBytes(byte[] value) throws IOException {
        if (value != null) {
            dataOutputStream.writeInt(value.length);
            dataOutputStream.write(value, 0, value.length);
        }
    }
}
