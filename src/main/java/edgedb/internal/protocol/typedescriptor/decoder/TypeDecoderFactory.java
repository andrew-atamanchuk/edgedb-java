package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface TypeDecoderFactory {

    public boolean decodeDescriptors(CommandDataDescriptor cdd);
    public boolean decodeInputDescriptors(ByteBuffer bb);
    public boolean decodeOutputDescriptors(ByteBuffer bb);
    public void setInputTypeDescId(UUID uuid);
    public void setOutputTypeDescId(UUID uuid);

}
