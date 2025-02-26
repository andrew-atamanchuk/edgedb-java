package edgedb.connection;

import edgedb.connectionparams.ConnectionParams;
import edgedb.exceptions.EdgeDBIncompatibleDriverException;
import edgedb.exceptions.EdgeDBInternalErrException;

import java.io.IOException;

public interface IConnection extends IChannel, Query, IQueryV2{
    //public IConnection connect(ConnectionParams connectionParams) throws IOException, EdgeDBInternalErrException;
    public IConnection createClientSocket(ConnectionParams connectionParams) throws IOException;
    public void terminate() throws IOException;
    public void initiateHandshake(String user, String database) throws IOException;
    public void handleHandshake() throws IOException;

}
