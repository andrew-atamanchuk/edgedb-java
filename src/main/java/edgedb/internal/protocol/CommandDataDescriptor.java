package edgedb.internal.protocol;

import lombok.Data;

import java.util.UUID;

import static edgedb.internal.protocol.constants.MessageType.COMMAND_DATA_DESCRIPTOR;

@Data
public class CommandDataDescriptor implements ServerProtocolBehaviour{
    byte mType = COMMAND_DATA_DESCRIPTOR;
    int messageLength;
    short headersLength;
    Header[] headers;
    long capabilities;
    byte result_cardinality;
    UUID input_typedesc_id;
    byte[] input_typedesc;
    UUID output_typedesc_id;
    byte[] output_typedesc;
}
