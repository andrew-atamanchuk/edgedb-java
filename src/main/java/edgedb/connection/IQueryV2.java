package edgedb.connection;

import edgedb.client.ResultSet;
import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.typedescriptor.decoder.ITypeDescriptorHolder;

import java.nio.ByteBuffer;

public interface IQueryV2 {
    public CommandDataDescriptor sendParseV2(ITypeDescriptorHolder desc_holder);
    public ResultSet sendExecuteV2(ITypeDescriptorHolder desc_holder, ByteBuffer args_bb);
}
