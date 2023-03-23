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

import static edgedb.internal.protocol.constants.MessageType.EXECUTE;

@Data
@Slf4j
public class ExecuteV2 implements BufferWritable, ClientProtocolBehaviour {
    byte mType = EXECUTE;
    int messageLength;
    short headersLength;
    Header[] headers;
    byte ioFormat;
    byte expectedCardinality;
    String command;
    long allowed_capabilities = Capability.ALL.value;
    long compilation_flags = CompilationFlag.INJECT_OUTPUT_TYPE_IDS.value;
    long implicit_limit = 1000 * 1024;
    UUID state_typedesc_id = new UUID(0, 0);
    byte[] state_data = new byte[4];

    UUID input_typedesc_id = new UUID(0, 0);
    UUID output_typedesc_id = new UUID(0, 0);
    byte[] arguments = new byte[0];

    enum CompilationFlag {
        INJECT_OUTPUT_TYPE_IDS(0x1),
        INJECT_OUTPUT_TYPE_NAMES(0x2),
        INJECT_OUTPUT_OBJECT_IDS(0x4);

        long value;

        CompilationFlag(long value){
            this.value = value;
        }
    };
    public ExecuteV2(char IOFormat, char expectedCardinality, String command) {
        this.headersLength = (short) 0;
        this.ioFormat = (byte) IOFormat;
        this.expectedCardinality = (byte) expectedCardinality;
        this.command = command;
        this.messageLength = calculateMessageLength();
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

        length += messageLengthCalculator.calculate(input_typedesc_id);
        length += messageLengthCalculator.calculate(output_typedesc_id);
        length += messageLengthCalculator.calculate(arguments);

        return length;
    }

    @Override
    public ByteBuffer write(ByteBuffer destination) throws IOException {
        log.info("Client Handshake Buffer Writer");
        IWriteHelper helper = new BufferWriterHelper(destination);
        return write(helper,destination);
    }

    @Override
    public ByteBuffer write(IWriteHelper helper, ByteBuffer destination) throws IOException {
        this.messageLength = calculateMessageLength();

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

        helper.writeUUID(input_typedesc_id);
        helper.writeUUID(output_typedesc_id);
        helper.writeBytes(arguments);

        return destination;
    }

    public void setArguments(ByteBuffer bb){
        if(bb != null && bb.remaining() > 0) {
            arguments = new byte[bb.remaining()];
            bb.get(arguments, 0, bb.remaining());
        }
    }
}
