package edgedb.internal.protocol;

import lombok.Data;

import java.util.UUID;

import static edgedb.internal.protocol.constants.MessageType.COMMAND_COMPLETE;

@Data
public class CommandComplete implements ServerProtocolBehaviour {
    byte mType = COMMAND_COMPLETE;
    int messageLength;
    short numAnnotations;
    Header[] annotations;
    long capabilities;
    String status;
    UUID stateTypedescId;
    byte[] stateData;
}
