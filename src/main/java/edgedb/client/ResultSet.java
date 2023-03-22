package edgedb.client;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.DataResponse;

public interface ResultSet {
    public void addResultData(DataResponse dataResponse);
    public void setCommandDataDescriptor(CommandDataDescriptor descriptor);
}
