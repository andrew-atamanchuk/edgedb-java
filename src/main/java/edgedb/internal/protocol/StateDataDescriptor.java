package edgedb.internal.protocol;

import lombok.Data;

import java.util.UUID;

import static edgedb.internal.protocol.constants.MessageType.STATE_DATA_DESCRIPTION;

@Data
public class StateDataDescriptor implements ServerProtocolBehaviour {

    byte mType = STATE_DATA_DESCRIPTION;
    int messageLength;
    UUID typedescId;
    byte[] typedesc;
}
