package edgedb.internal.protocol.typedescriptor;

public enum Cardinality {
    NO_RESULT((byte)0x6e),
    AT_MOST_ONE((byte)0x6f),
    ONE((byte)0x41),
    MANY((byte)0x6d),
    AT_LEAST_ONE((byte)0x4d);

    byte value;
    Cardinality(byte value){
        this.value = value;
    }

    public static Cardinality replacementValueOf(byte b) {
        for(Cardinality num : Cardinality.values())
            if(b == num.value)
                return num;
        throw new RuntimeException("Your byte " + b + " was not a backing value for Cardinality.");
    }
}
