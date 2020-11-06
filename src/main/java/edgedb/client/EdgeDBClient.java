package edgedb.client;

import edgedb.exceptions.*;
import edgedb.internal.parser.JsonProcessor;
import edgedb.internal.pipes.connect.ConnectionPipe;
import edgedb.internal.pipes.executescript.ExecuteScriptPipe;
import edgedb.internal.pipes.granularflow.GranularFlowPipe;
import edgedb.internal.pipes.terminate.TerminatePipe;
import edgedb.internal.protocol.constants.Cardinality;
import edgedb.internal.protocol.constants.IOFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
public class EdgeDBClient {
    Connection connection;
    private SocketStream socketStream;

    public EdgeDBClient withDSN(String dsn) {
        log.debug("Trying to connect with client {}", dsn);
        connection = new Connection(dsn);
        log.debug("Connection {}", connection);
        return this;
    }


    public EdgeDBClient connect() throws Throwable {
        log.debug("Connection to EdgeDB started with connection {}", connection);
        ConnectionPipe connectionPipe = new ConnectionPipe(connection);
        socketStream = connectionPipe.open();
        log.debug("Connection to EdgeDB was successful");
        return this;
    }

    public String executeScript(String script) throws EdgeDBServerException, EdgeDBInternalErrException, IOException, InterruptedException {
        log.debug("Started executing script {}", script);
        ExecuteScriptPipe executeScriptPipe = new ExecuteScriptPipe(socketStream);
        executeScriptPipe.write(script);
        Thread.sleep(10000);
        String result = executeScriptPipe.read();
        log.debug("Result from the Execute Script {}", result);
        return "";
    }

    public String execute(String query) throws IOException, EdgeDBInternalErrException, EdgeDBCommandException {
        log.debug("Started executing statement {}", query);
        GranularFlowPipe granularFlowPipe = new GranularFlowPipe(socketStream);
        granularFlowPipe.setup(query);
        String result = granularFlowPipe.execute();
        return result;
    }

    //TODO: Should this return boolean flag
    public void terminate() throws IOException {
        log.debug("Trying to terminate EdgeDB connection");
        TerminatePipe terminatePipe = new TerminatePipe(socketStream);
        terminatePipe.terminate();
    }


    public String Query(String query) throws IOException, EdgeDBInternalErrException {
        return null;
    }

    public String QueryOne(String query) throws IOException, EdgeDBInternalErrException {
        return null;
    }


    public String queryOneJSON(String query) throws IOException, EdgeDBInternalErrException, EdgeDBCommandException {
        log.debug("Started executing QueryOneJSON {}", query);
        GranularFlowPipe granularFlowPipe = new GranularFlowPipe(socketStream);
        granularFlowPipe.setup(query, Cardinality.ONE, IOFormat.JSON);
        String result = granularFlowPipe.execute();
        return result;
    }

    public <T> T queryOneJSON(String query,Class<T> classType) throws IOException, EdgeDBInternalErrException, EdgeDBCommandException {
        log.debug("Started executing QueryOneJSON {}", query);
        GranularFlowPipe granularFlowPipe = new GranularFlowPipe(socketStream);
        granularFlowPipe.setup(query, Cardinality.ONE, IOFormat.JSON);
        String result = granularFlowPipe.execute();
        return JsonProcessor.unmarshalToType(result,classType);
    }

    public String queryJSON(String query) throws IOException, EdgeDBInternalErrException, EdgeDBCommandException {
        log.debug("Started executing QueryOneJSON {}", query);
        GranularFlowPipe granularFlowPipe = new GranularFlowPipe(socketStream);
        granularFlowPipe.setup(query, Cardinality.MANY, IOFormat.JSON);
        String result = granularFlowPipe.execute();
        return result;
    }

    public <T> List<T> queryJSON(String query, Class<T> classType) throws IOException, EdgeDBInternalErrException, EdgeDBCommandException {
        log.debug("Started executing QueryOneJSON {}", query);
        GranularFlowPipe granularFlowPipe = new GranularFlowPipe(socketStream);
        granularFlowPipe.setup(query, Cardinality.MANY, IOFormat.JSON);
        String result = granularFlowPipe.execute();
        return JsonProcessor.unmarshalToList(result,classType);
    }

}
