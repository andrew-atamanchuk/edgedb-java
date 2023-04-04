package edgedb.internal.protocol.server.readerv2;

import edgedb.internal.protocol.StateDataDescriptor;
import edgedb.internal.protocol.server.readerhelper.IReaderHelper;
import edgedb.internal.protocol.utility.UUIDUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
public class StateDataDescriptorReaderV2 implements ProtocolReader{
    IReaderHelper readerHelper;

    @Override
    public StateDataDescriptor read(ByteBuffer readBuffer) {
        StateDataDescriptor message = new StateDataDescriptor();

        int messageLength = readerHelper.readUint32();
        message.setMessageLength(messageLength);
        readerHelper.setMessageLength(messageLength);

        message.setTypedescId(UUIDUtils.convertBytesToUUID(readerHelper.readUUID()));
        message.setTypedesc(readerHelper.readBytes());

        return message;
    }
}
