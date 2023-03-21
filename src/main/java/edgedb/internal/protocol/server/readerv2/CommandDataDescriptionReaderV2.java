package edgedb.internal.protocol.server.readerv2;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.Header;
import edgedb.internal.protocol.server.readerhelper.IReaderHelper;
import edgedb.internal.protocol.utility.UUIDUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
public class CommandDataDescriptionReaderV2 implements ProtocolReader {
    IReaderHelper readerHelper;

    public CommandDataDescriptor read(ByteBuffer readBuffer) {

        CommandDataDescriptor comm = new CommandDataDescriptor();

        int messageLength = readerHelper.readUint32();
        comm.setMessageLength(messageLength);
        readerHelper.setMessageLength(messageLength);

        short headerLength = readerHelper.readUint16();
        comm.setHeadersLength(headerLength);
        Header[] headers = new Header[headerLength];
        HeaderReader headerReader = new HeaderReader(readerHelper);
        for (int i = 0; i < headerLength; i++) {
            headers[i] = headerReader.read(readBuffer);
        }
        comm.setCapabilities(readerHelper.readUint64());
        comm.setResult_cardinality(readerHelper.readByte());
        comm.setInput_typedesc_id(UUIDUtils.convertBytesToUUID(readerHelper.readUUID()));
        comm.setInput_typedesc(readerHelper.readBytes());
        comm.setOutput_typedesc_id(UUIDUtils.convertBytesToUUID(readerHelper.readUUID()));
        comm.setOutput_typedesc(readerHelper.readBytes());
        return comm;
    }
}
