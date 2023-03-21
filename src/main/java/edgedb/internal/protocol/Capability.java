package edgedb.internal.protocol;

public enum Capability {
    MODIFICATIONS(0x1),
    SESSION_CONFIG(0x2),
    TRANSACTION(0x4),
    DDL(0x8),
    PERSISTENT_CONFIG(0x10),
    ALL(0xffffffffffffffffL);

    long value;

    Capability(long value){
        this.value = value;
    }
}
