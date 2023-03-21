package edgedb.internal.protocol.typedescriptor;

public enum RangeFlag {
    // Empty range.
    EMPTY((byte)0x0001),

    // Included lower boundary.
    LB_INC((byte)0x0002),

    // Included upper boundary.
    UB_INC((byte)0x0004),

    // Inifinity (excluded) lower boundary.
    LB_INF((byte)0x0008),

    // Infinity (excluded) upper boundary.
    UB_INF((byte)0x0010);

    public byte flag;

    RangeFlag(byte value){
        this.flag = value;
    }

    public static RangeFlag replacementValueOf(byte b) {
        for(RangeFlag num : RangeFlag.values())
            if(b == num.flag)
                return num;
        throw new RuntimeException("Your byte " + b + " was not a backing value for RangeFlags.");
    }
}
