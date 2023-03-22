package edgedb.connection;

import edgedb.client.ResultSet;
import edgedb.internal.protocol.CommandDataDescriptor;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface IQueryV2 {
    public CommandDataDescriptor sendParseV2(char IOFormat, char cardinality, String command);
    public ResultSet sendExecuteV2(char IOFormat, char cardinality, String command, UUID input_typedesc_id, UUID output_typedesc_id, ByteBuffer args_bb);
}
