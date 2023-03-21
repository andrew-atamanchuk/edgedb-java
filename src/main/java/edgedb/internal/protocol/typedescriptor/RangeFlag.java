package edgedb.internal.protocol.typedescriptor;

public enum RangeFlag {
    // Empty range.
    EMPTY((byte)0x01),

    // Included lower boundary.
    LB_INC((byte)0x02),

    // Included upper boundary.
    UB_INC((byte)0x04),

    // Inifinity (excluded) lower boundary.
    LB_INF((byte)0x08),

    // Infinity (excluded) upper boundary.
    UB_INF((byte)0x10);

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
