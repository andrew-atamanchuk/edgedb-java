package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface TypeDecoderFactory {

    public boolean decodeDescriptors(CommandDataDescriptor cdd, ITypeDescriptorHolder desc_holder);
}
