package edgedb.internal.protocol;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static edgedb.internal.protocol.constants.MessageType.PARAMETER_STATUS;

@Data
@Slf4j
public class ParameterStatus implements ServerProtocolBehaviour {
    byte mType = (int) PARAMETER_STATUS;
    int messageLength;
    byte[] name;
    byte[] value;
}
