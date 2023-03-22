package edgedb.connection;

import edgedb.client.ResultSet;
import edgedb.connectionparams.ConnectionParams;
import edgedb.internal.protocol.CommandDataDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.UUID;

@Slf4j
public class NonBlockingConnection implements IConnection {

    SocketChannel clientChannel;
    Selector selector;
    byte[] serverKey;


    public void handleHandshake(){

    }


    @Override
    public IConnection createClientSocket(ConnectionParams connectionParams) throws IOException {
        clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);

        if (!clientChannel.connect(new InetSocketAddress(connectionParams.getHost(), connectionParams.getPort()))) {
            log.info("Trying to connect ...");
            while (!clientChannel.finishConnect());

            log.info("Connection Successful....");
        }

        selector = Selector.open();
        SelectionKey readKey = clientChannel.register(selector, SelectionKey.OP_READ);
        SelectionKey writeKey = clientChannel.register(selector, SelectionKey.OP_WRITE);
        return this;
    }

    @Override
    public void terminate() throws IOException {

    }

    @Override
    public void initiateHandshake(String user, String database) {

    }

    @Override
    public ResultSet query(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet queryOne(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet queryJSON(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(String query){
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketChannel getChannel() {
        return null;
    }

    @Override
    public CommandDataDescriptor sendParseV2(char IOFormat, char cardinality, String command) {
        return null;
    }

    @Override
    public ResultSet sendExecuteV2(char IOFormat, char cardinality, String command, UUID input_typedesc_id, UUID output_typedesc_id, ByteBuffer args_bb) {
        return null;
    }
}
