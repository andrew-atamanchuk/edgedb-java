package edgedb.internal.protocol.typedescriptor;

public interface IDescType {
    public static final byte BASE_SCALAR_DESC_TYPE = 2;
    public static final byte SET_DESC_TYPE = 0;
    public static final byte OBJECT_SHAPE_DESC_TYPE = 1;
    public static final byte SCALAR_DESC_TYPE = 3;
    public static final byte TUPLE_DESC_TYPE = 4;
    public static final byte NAMED_TUPLE_DESC_TYPE = 5;
    public static final byte ARRAY_DESC_TYPE = 6;
    public static final byte ENUMERATION_DESC_TYPE = 7;
    public static final byte INPUT_SHAPE_DESC_TYPE = 8;
    public static final byte RANGE_DESC_TYPE = 9;
    public static final byte SCALAR_ANNOTATION_DESC_TYPE = (byte)0xff;
    //type of descriptor in range 0x80..0xfe from 128-254
    public static final byte TYPE_ANNOTATION_DESC_TYPE_START = (byte)0x80;
    public static final byte TYPE_ANNOTATION_DESC_TYPE_END = (byte)0xfe;
}
