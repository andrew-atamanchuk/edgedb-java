package edgedb.internal.protocol.server.readerv2;

import edgedb.internal.protocol.ParameterStatus;
import edgedb.internal.protocol.SyncMessage;
import edgedb.internal.protocol.server.readerhelper.IReaderHelper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
public class ParameterStatusReaderV2 implements ProtocolReader {
    IReaderHelper readerHelper;

    @Override
    public ParameterStatus read(ByteBuffer readBuffer) {
        ParameterStatus message = new ParameterStatus();
        int messageLength = readerHelper.readUint32();
        message.setMessageLength(messageLength);
        readerHelper.setMessageLength(messageLength);

        message.setName(readerHelper.readBytes());
        message.setValue(readerHelper.readBytes());

        return message;
    }
}
