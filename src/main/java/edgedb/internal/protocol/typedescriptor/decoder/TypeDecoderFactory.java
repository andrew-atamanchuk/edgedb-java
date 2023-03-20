package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

import java.nio.ByteBuffer;

public interface TypeDecoderFactory {

    public boolean decodeDescriptors(ByteBuffer bb);
    public TypeDescriptor getTypeDescriptor(ByteBuffer bb);


}
