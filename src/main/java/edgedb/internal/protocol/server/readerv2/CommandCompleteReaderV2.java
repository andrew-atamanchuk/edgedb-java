package edgedb.internal.protocol.server.readerv2;

import edgedb.internal.protocol.CommandComplete;
import edgedb.internal.protocol.Header;
import edgedb.internal.protocol.server.readerhelper.IReaderHelper;
import edgedb.internal.protocol.utility.UUIDUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
public class CommandCompleteReaderV2 implements ProtocolReader {

    IReaderHelper readerHelper;

    public CommandComplete read(ByteBuffer readBuffer) {

        CommandComplete commandComplete = new CommandComplete();

        int messageLength = readerHelper.readUint32();
        commandComplete.setMessageLength(messageLength);
        readerHelper.setMessageLength(messageLength);

        short headerLength = readerHelper.readUint16();
        commandComplete.setNumAnnotations(headerLength);
        Header[] headers = new Header[headerLength];
        HeaderReader headerReader = new HeaderReader(readerHelper);
        for (int i = 0; i < headerLength; i++) {
            headers[i] = headerReader.read(readBuffer);
        }
        commandComplete.setAnnotations(headers);
        commandComplete.setCapabilities(readerHelper.readUint64());
        commandComplete.setStatus(readerHelper.readString());
        commandComplete.setStateTypedescId(UUIDUtils.convertBytesToUUID(readerHelper.readUUID()));
        commandComplete.setStateData(readerHelper.readBytes());

        return commandComplete;
    }
}

