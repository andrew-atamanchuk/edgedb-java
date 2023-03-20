package edgedb.internal.protocol;

import edgedb.internal.protocol.client.writerV2.BufferWritable;
import edgedb.internal.protocol.client.writerhelper.BufferWriterHelper;
import edgedb.internal.protocol.client.writerhelper.IWriteHelper;
import edgedb.internal.protocol.utility.MessageLengthCalculator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import static edgedb.internal.protocol.constants.MessageType.PREPARE;

@Data
@Slf4j
public class Prepare implements BufferWritable, ClientProtocolBehaviour {
    byte mType = PREPARE;
    int messageLength;
    short headersLength;
    Header[] headers;
    byte ioFormat;
    byte expectedCardinality;
    byte[] statementName;
    String command;
    long allowed_capabilities = Capability.ALL.value;
    long compilation_flags = CompilationFlag.INJECT_OUTPUT_TYPE_IDS.value;
    long implicit_limit = 1000 * 1024;
    UUID state_typedesc_id = new UUID(0, 0);
    byte[] state_data = new byte[4];

    public Prepare(char IOFormat, char expectedCardinality, String command) {
        this.headersLength = (short) 0;
        this.ioFormat = (byte) IOFormat;
        this.expectedCardinality = (byte) expectedCardinality;
        this.statementName = "".getBytes();
        this.command = command;
        this.messageLength = calculateMessageLength();
        //this.state_typedesc_id = UUID.randomUUID();
    }

    @Override
    public int calculateMessageLength() {
        int length = 0;
        MessageLengthCalculator messageLengthCalculator = new MessageLengthCalculator();
        length += messageLengthCalculator.calculate(messageLength);

        length += messageLengthCalculator.calculate(headersLength);
        for (int i = 0; i < headersLength; i++) {
            length += headers[i].calculateMessageLength();
        }

        length += messageLengthCalculator.calculate(allowed_capabilities);
        length += messageLengthCalculator.calculate(compilation_flags);
        length += messageLengthCalculator.calculate(implicit_limit);

        length += messageLengthCalculator.calculate(ioFormat);
        length += messageLengthCalculator.calculate(expectedCardinality);
        length += messageLengthCalculator.calculate(command);
        length += messageLengthCalculator.calculate(state_typedesc_id);
        length += messageLengthCalculator.calculate(state_data);

        return length;
    }

    @Override
    public ByteBuffer write(ByteBuffer destination) throws IOException {
        this.messageLength = calculateMessageLength();
        log.info("Client Handshake Buffer Writer");
        IWriteHelper helper = new BufferWriterHelper(destination);
        return write(helper,destination);
    }

    @Override
    public ByteBuffer write(IWriteHelper helper, ByteBuffer destination) throws IOException {
        helper.writeUint8(mType);
        helper.writeUint32(messageLength);

        helper.writeUint16(headersLength);

        for(int i=0; i<(int)headersLength; i++){
            headers[i].write(helper,destination);
        }

        helper.writeUint64(allowed_capabilities);
        helper.writeUint64(compilation_flags);
        helper.writeUint64(implicit_limit);

        helper.writeUint8(ioFormat);
        helper.writeUint8(expectedCardinality);
        helper.writeString(command);
        helper.writeUUID(state_typedesc_id);
        helper.writeBytes(state_data);
        return destination;
    }
}
