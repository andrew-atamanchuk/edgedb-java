package edgedb.client;

public class AsyncConnection extends BaseConnection{

    protected AsyncConnection(String dsn) {
        super(dsn);
    }

    @Override
    public void connect(String dsn) {

    }

    @Override
    public void terminate() {

    }
}
