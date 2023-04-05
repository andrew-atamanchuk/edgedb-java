package edgedb.query;

public interface IQueryCommand {
    public String command();
    public char outputFormat();
    public char cardinality();
}
