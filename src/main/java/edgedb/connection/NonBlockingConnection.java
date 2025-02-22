package edgedb.connection;

import edgedb.client.ResultSet;
import edgedb.client.ResultSetImpl;
import edgedb.connectionparams.ConnectionParams;
import edgedb.exceptions.IExceptionFromErrorResponseBuilderImpl;
import edgedb.exceptions.clientexception.ClientException;
import edgedb.exceptions.constants.Severity;
import edgedb.internal.buffer.SingletonBuffer;
import edgedb.internal.pipes.SyncFlow.SyncPipe;
import edgedb.internal.pipes.SyncFlow.SyncPipeImpl;
import edgedb.internal.pipes.authenticationflow.AutheticationFlowScramSASL;
import edgedb.internal.pipes.authenticationflow.IScramSASLAuthenticationFlow;
import edgedb.internal.pipes.connect.ConnectionPipe;
import edgedb.internal.pipes.connect.IConnectionPipe;
import edgedb.internal.pipes.granularflow.GranularFlowPipeV2;
import edgedb.internal.pipes.granularflow.IGranularFlowPipe;
import edgedb.internal.protocol.*;
import edgedb.internal.protocol.client.writerV2.ChannelProtocolWritableImpl;
import edgedb.internal.protocol.server.readerfactory.ChannelProtocolReaderFactoryImpl;
import edgedb.internal.protocol.server.readerv2.BufferReader;
import edgedb.internal.protocol.server.readerv2.BufferReaderImpl;
import edgedb.internal.protocol.server.readerv2.ProtocolReader;
import edgedb.internal.protocol.typedescriptor.decoder.ITypeDescriptorHolder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.UUID;

import static edgedb.client.ClientConstants.MAJOR_VERSION;
import static edgedb.client.ClientConstants.MINOR_VERSION;
import static edgedb.exceptions.constants.ClientErrors.INCOMPATIBLE_DRIVER;
import static edgedb.internal.protocol.constants.TransactionState.*;

@Slf4j
public class NonBlockingConnection implements IConnection {

    SocketChannel clientChannel;
    Selector selector;
    byte[] serverKey;

    private IGranularFlowPipe granularFlowPipe = null;
    ConnectionParams connectionParams;

    @Override
    public void handleHandshake() throws IOException {
        tryHandleHandshake();
        log.info("Connection Successful, Ready for command.");
    }


    @Override
    public IConnection createClientSocket(ConnectionParams connectionParams) throws IOException {
        log.info("Trying to create Client Socket");
        this.connectionParams = connectionParams;

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
        IConnectionPipe connectionPipeV2 = new ConnectionPipe(
                new ChannelProtocolWritableImpl(getChannel()));

        connectionPipeV2.sendTerminate(new Terminate());
    }

    @Override
    public void initiateHandshake(String user, String database) throws IOException {
        log.info("Initiating Client Handshake");
        ClientHandshake clientHandshakeMessage = new ClientHandshake(user, database);
        IConnectionPipe connectionPipeV2 = new ConnectionPipe(
                new ChannelProtocolWritableImpl(getChannel()));
        connectionPipeV2.sendClientHandshake(clientHandshakeMessage);
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
        return clientChannel;
    }

    @Override
    public CommandDataDescriptor sendParseV2(ITypeDescriptorHolder desc_holder) {
        granularFlowPipe = new GranularFlowPipeV2(
                new ChannelProtocolWritableImpl(getChannel()));

        Prepare prepareMessage = new Prepare(desc_holder.outputFormat(), desc_holder.cardinality(), desc_holder.command());
        granularFlowPipe.sendPrepareMessage(prepareMessage);

        return readCommandDataDescriptor(granularFlowPipe, prepareMessage);
    }

    @Override
    public ResultSet sendExecuteV2(ITypeDescriptorHolder desc_holder, ByteBuffer args_bb) {
        if(granularFlowPipe == null) {
            granularFlowPipe = new GranularFlowPipeV2(
                    new ChannelProtocolWritableImpl(getChannel()));
        }

        ExecuteV2 exec_message = new ExecuteV2(desc_holder.outputFormat(), desc_holder.cardinality(), desc_holder.command());
        exec_message.setInput_typedesc_id(desc_holder.getInputTypeDescId() == null ? new UUID(0, 0) : desc_holder.getInputTypeDescId());
        exec_message.setOutput_typedesc_id(desc_holder.getOutputTypeDescId() == null ? new UUID(0, 0) : desc_holder.getOutputTypeDescId());
        exec_message.setArguments(args_bb);
        granularFlowPipe.sendExecuteMessageV2(exec_message);
        return readDataResponse();
    }

    private <T extends ServerProtocolBehaviour> void tryHandleHandshake() throws IOException {
        log.debug("Trying to read response for client handshake");

        ByteBuffer readBuffer = SingletonBuffer.getInstance().getBuffer();
        BufferReader bufferReader = new BufferReaderImpl(getChannel());

        boolean is_finished = false;

        while (!is_finished) {
            if(bufferReader.read(readBuffer) < 0){
                is_finished = true;
                connectionLost();
                break;
            }

            while (readBuffer.hasRemaining()) {
                byte mType = readBuffer.get();
                if (readBuffer.remaining() >= 4) {
                    int bb_pos = readBuffer.position();
                    int msg_size = readBuffer.getInt();
                    readBuffer.position(bb_pos);
                    if (readBuffer.remaining() < msg_size) {
                        readBuffer.position(readBuffer.position() - 1);
                        break;
                    }
                } else {
                    readBuffer.position(readBuffer.position() - 1);
                    break;
                }

                ProtocolReader reader = new ChannelProtocolReaderFactoryImpl(readBuffer)
                        .getProtocolReader((char) mType, readBuffer);

                T response = reader.read(readBuffer);
                log.info(" Response Found was {}", response.toString());
                if (response instanceof ErrorResponse) {
                    log.debug("ErrorResponse is an Instance Of ServerHandshake {}", response);
                    System.out.println("ErrorResponse: " + response);
                } else if (response instanceof ServerHandshakeBehaviour) {
                    ServerHandshakeBehaviour serverHandshake = (ServerHandshakeBehaviour) response;
                    log.debug("Response is an Instance Of ServerHandshake {}", serverHandshake);
                    String message = String.format("Incompatible driver expected Minor Version %s and Major Version %s,found Minor Version %s and Major Version %s", MAJOR_VERSION, MINOR_VERSION, serverHandshake.getMajorVersion(), serverHandshake.getMinorVersion());
                    throw new ClientException(INCOMPATIBLE_DRIVER, message, Severity.ERROR);
                } else if (response instanceof ReadyForCommand) {
                    ReadyForCommand readyForCommand = (ReadyForCommand) response;
                    log.debug("Response is an Instance Of ReadyForCommand {}", readyForCommand);

                    switch (readyForCommand.getTransactionState()) {
                        case (int) IN_FAILED_TRANSACTION:
                            SyncPipe syncPipe = new SyncPipeImpl(
                                    new ChannelProtocolWritableImpl(getChannel()));
                            syncPipe.sendSyncMessage();
                            break;
                        case (int) IN_TRANSACTION:
                            break;
                        case (int) NOT_IN_TRANSACTION:
                            return;
                    }
                } else if (response instanceof ServerAuthenticationBehaviour) {
                    ServerAuthenticationBehaviour serverAuthenticationBehaviour = (ServerAuthenticationBehaviour) response;

                    if (serverAuthenticationBehaviour.isAuthenticationOkMessage()) {
                        log.info("Authentication Successful");
                        return;
                    } else if (serverAuthenticationBehaviour.isAuthenticationRequiredSASLMessage()) {
                        authenticateSASL();
                        continue;
                    }
                } else if (response instanceof ServerKeyDataBehaviour) {
                    continue;
                }
            }

            readBuffer.compact();
        }
    }

    public <T extends ServerProtocolBehaviour> void authenticateSASL() throws IOException {

        IScramSASLAuthenticationFlow authenticationFlow = new AutheticationFlowScramSASL(
                new ChannelProtocolWritableImpl(getChannel()));
        authenticationFlow.sendAuthenticationSASLClientFirstMessage(connectionParams.getUser());

        ByteBuffer readBuffer = SingletonBuffer.getInstance().getBuffer();
        BufferReader bufferReader = new BufferReaderImpl(getChannel());

        boolean is_finished = false;

        while (!is_finished) {
            if(bufferReader.read(readBuffer) < 0){
                is_finished = true;
                connectionLost();
                break;
            }

            while (readBuffer.hasRemaining()) {
                byte mType = readBuffer.get();
                if (readBuffer.remaining() >= 4) {
                    int bb_pos = readBuffer.position();
                    int msg_size = readBuffer.getInt();
                    readBuffer.position(bb_pos);
                    if (readBuffer.remaining() < msg_size) {
                        readBuffer.position(readBuffer.position() - 1);
                        break;
                    }
                } else {
                    readBuffer.position(readBuffer.position() - 1);
                    break;
                }

                ProtocolReader reader = new ChannelProtocolReaderFactoryImpl(readBuffer)
                        .getProtocolReader((char) mType, readBuffer);

                T response = reader.read(readBuffer);

                log.info("Response Found was {}", response.toString());
                if (response instanceof ServerAuthenticationBehaviour) {
                    ServerAuthenticationBehaviour serverAuthenticationBehaviour = (ServerAuthenticationBehaviour) response;
                    if (serverAuthenticationBehaviour.isAuthenticationSASLContinueMessage()) {
                        authenticationFlow.sendAuthenticationSASLClientFinalMessage(serverAuthenticationBehaviour, connectionParams.getPassword());
                        //TODO will be tested
                        is_finished = true;
                        break;
                    }
                } else if (response instanceof ErrorResponse) {
                    ErrorResponse errorResponse = (ErrorResponse) response;
                    throw IExceptionFromErrorResponseBuilderImpl.getExceptionFromError(errorResponse);
                }

            }

            readBuffer.compact();
        }

        readClientFinalMessage(readBuffer, bufferReader);
    }

    private <T extends ServerProtocolBehaviour> void readClientFinalMessage(ByteBuffer readBuffer, BufferReader bufferReader) throws IOException {
        boolean is_finished = false;

        while (!is_finished) {
            if(bufferReader.read(readBuffer) < 0){
                is_finished = true;
                connectionLost();
                break;
            }

            while (readBuffer.hasRemaining()) {
                byte mType = readBuffer.get();
                if (readBuffer.remaining() >= 4) {
                    int bb_pos = readBuffer.position();
                    int msg_size = readBuffer.getInt();
                    readBuffer.position(bb_pos);
                    if (readBuffer.remaining() < msg_size) {
                        readBuffer.position(readBuffer.position() - 1);
                        break;
                    }
                } else {
                    readBuffer.position(readBuffer.position() - 1);
                    break;
                }

                ProtocolReader reader = new ChannelProtocolReaderFactoryImpl(readBuffer)
                        .getProtocolReader((char) mType, readBuffer);

                T response = reader.read(readBuffer);

                log.info("Response Found was {}", response.toString());
                if (response instanceof ServerAuthenticationBehaviour) {
                    ServerAuthenticationBehaviour serverAuthenticationBehaviour = (ServerAuthenticationBehaviour) response;

                    if (serverAuthenticationBehaviour.isAuthenticationSASLFinal()) {
                        // Expect AuthenticationOk message
                        continue;
                    } else if (serverAuthenticationBehaviour.isAuthenticationOkMessage()) {
                        is_finished = true;
                        break;
                    }
                } else if (response instanceof ErrorResponse) {
                    ErrorResponse errorResponse = (ErrorResponse) response;
                    throw IExceptionFromErrorResponseBuilderImpl.getExceptionFromError(errorResponse);
                }

            }

            readBuffer.compact();
        }
    }

    protected <T extends ServerProtocolBehaviour> CommandDataDescriptor readCommandDataDescriptor(IGranularFlowPipe granularFlowPipe, Prepare prepareMessage){
        log.debug("Reading prepare complete");
        BufferReader bufferReader = new BufferReaderImpl(clientChannel);
        ByteBuffer readBuffer = SingletonBuffer.getInstance().getBuffer();

        CommandDataDescriptor result_cdd = null;

        boolean is_finished = false;

        while (!is_finished) {
            if(bufferReader.read(readBuffer) < 0){
                is_finished = true;
                connectionLost();
                break;
            }

            while (readBuffer.hasRemaining()) {
                byte mType = readBuffer.get();
                if (readBuffer.remaining() >= 4) {
                    int bb_pos = readBuffer.position();
                    int msg_size = readBuffer.getInt();
                    readBuffer.position(bb_pos);
                    if (readBuffer.remaining() < msg_size) {
                        readBuffer.position(readBuffer.position() - 1);
                        break;
                    }
                } else {
                    readBuffer.position(readBuffer.position() - 1);
                    break;
                }

                ProtocolReader reader = new ChannelProtocolReaderFactoryImpl(readBuffer)
                        .getProtocolReader((char) mType, readBuffer);

                if (reader == null)
                    continue;

                T response = reader.read(readBuffer);

                if (response instanceof CommandDataDescriptor) {
                    log.debug("Response is an Instance Of CommandDataDescriptor {}", (CommandDataDescriptor) response);
                    result_cdd = (CommandDataDescriptor) response;
                }

            /*if (response instanceof PrepareComplete) {
                log.debug("Response is an Instance Of PrepareComplete {}", (PrepareComplete) response);
                return (PrepareComplete) response;
            }*/

                if (response instanceof ErrorResponse) {
                    throw IExceptionFromErrorResponseBuilderImpl.getExceptionFromError((ErrorResponse) response);
                }

                if (response instanceof ServerKeyDataBehaviour) {
                    log.debug("Response is an Instance Of Error {}", (ServerKeyDataBehaviour) response);
                    ServerKeyDataBehaviour serverKeyData = (ServerKeyDataBehaviour) response;
                    this.serverKey = serverKeyData.getData();
                    continue;
                }

                if (response instanceof ReadyForCommand) {
                    log.debug("Response is an Instance Of ReadyForCommand {}", (ReadyForCommand) response);
                    ReadyForCommand readyForCommand = (ReadyForCommand) response;

                    switch (readyForCommand.getTransactionState()) {
                        case (int) IN_FAILED_TRANSACTION:
                            log.info("In Failed Transaction");
                            //TODO: Coding to concrete implementation here. Watch out.
                            SyncPipe syncPipe = new SyncPipeImpl(
                                    new ChannelProtocolWritableImpl(getChannel()));
                            syncPipe.sendSyncMessage();
                            continue;
                        case (int) IN_TRANSACTION:
                            log.info("In Transaction");
                            throw new UnsupportedOperationException();
                        case (int) NOT_IN_TRANSACTION:
                            log.info("Not In Transaction");
                            break;
                    }
                    is_finished = true;
                    break;
                }
            }

            readBuffer.compact();
        }

        return result_cdd;
    }

    protected <T extends ServerProtocolBehaviour> ResultSet readDataResponse(){
        log.debug("Reading DataResponse");
        DataResponse dataResponse;
        BufferReader bufferReader = new BufferReaderImpl(getChannel());
        ByteBuffer readBuffer = SingletonBuffer.getInstance().getBuffer();

        ResultSet resultSet = new ResultSetImpl();
        boolean is_finished = false;

        while (!is_finished) {
            if(bufferReader.read(readBuffer) < 0){
                is_finished = true;
                connectionLost();
                break;
            }

            while (readBuffer.hasRemaining()) {
                byte mType = readBuffer.get();
                if (readBuffer.remaining() >= 4) {
                    int bb_pos = readBuffer.position();
                    int msg_size = readBuffer.getInt();
                    readBuffer.position(bb_pos);
                    if (readBuffer.remaining() < msg_size) {
                        readBuffer.position(readBuffer.position() - 1);
                        break;
                    }
                } else {
                    readBuffer.position(readBuffer.position() - 1);
                    break;
                }

                ProtocolReader reader = new ChannelProtocolReaderFactoryImpl(readBuffer)
                        .getProtocolReader((char) mType, readBuffer);

                if (reader == null)
                    continue;

                T response = reader.read(readBuffer);

                if (response != null) {
                    if (response instanceof DataResponse) {
                        log.debug("Response is an Instance Of DataResponse {}", (DataResponse) response);
                        dataResponse = (DataResponse) response;

                        resultSet.addResultData(dataResponse);

                        log.debug("Data Response :- {}", dataResponse);
                    }
                    if (response instanceof ReadyForCommand) {
                        log.debug("Response is an Instance Of ReadyForCommand {}", (ReadyForCommand) response);
                        ReadyForCommand readyForCommand = (ReadyForCommand) response;

                        switch (readyForCommand.getTransactionState()) {
                            case (int) IN_FAILED_TRANSACTION:
                                log.info("In Failed Transaction");
                                //TODO: Coding to concrete implementation here. Watch out.
                                SyncPipe syncPipe = new SyncPipeImpl(
                                        new ChannelProtocolWritableImpl(getChannel()));
                                syncPipe.sendSyncMessage();
                                continue;
                            case (int) IN_TRANSACTION:
                                log.info("In Transaction");
                                throw new UnsupportedOperationException();
                            case (int) NOT_IN_TRANSACTION:
                                log.info("Not In Transaction");
                                break;
                        }
                        is_finished = true;
                        break;
                    }
                }
            }

            readBuffer.compact();
        }
        return resultSet;
    }

    private void connectionLost(){

    }
}
