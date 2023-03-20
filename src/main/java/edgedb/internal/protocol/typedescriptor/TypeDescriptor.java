package edgedb.internal.protocol.typedescriptor;

import edgedb.internal.protocol.typedescriptor.decoder.ITypeDescriptorHolder;
import edgedb.internal.protocol.utility.UUIDUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class TypeDescriptor {
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


    protected ITypeDescriptorHolder descriptor_holder = null;
    protected final byte type;
    protected byte[] id = new byte[Long.SIZE * 2 / Byte.SIZE];
    protected UUID uuid;

    public TypeDescriptor(byte type){
        this.type = type;
    }

    abstract public Object decodeData(ByteBuffer bb, int length);

    public boolean parse(ByteBuffer bb){
        if(bb.remaining() > id.length) {
            int pos = bb.position();
            bb.get(id, 0, id.length);
            bb.position(pos);
            uuid = UUIDUtils.convertBytesToUUID(bb);
            return true;
        }
        return false;
    }

    public void setDescriptorHolder(ITypeDescriptorHolder holder){
        this.descriptor_holder = holder;
    }

    public static TypeDescriptor createInstanceFromBB(ByteBuffer bb){
        int bb_pos = bb.position();
        byte desc_type = bb.get();
        TypeDescriptor desc = null;
        switch (desc_type){
            case SET_DESC_TYPE: desc = new SetTypeDescriptor(); break;
            case OBJECT_SHAPE_DESC_TYPE: desc = new ObjectShapeDescriptor(); break;
            case BASE_SCALAR_DESC_TYPE: desc = new BaseScalarTypeDescriptor(); break;
            case SCALAR_DESC_TYPE: desc = new ScalarTypeDescriptor(); break;
            case TUPLE_DESC_TYPE: desc = new TupleTypeDescriptor(); break;
            case NAMED_TUPLE_DESC_TYPE: desc = new NamedTupleTypeDescriptor(); break;
            case ARRAY_DESC_TYPE: desc = new ArrayTypeDescriptor(); break;
            case ENUMERATION_DESC_TYPE: desc = new EnumerationTypeDescriptor(); break;
            case INPUT_SHAPE_DESC_TYPE: desc = new InputShapeTypeDescriptor(); break;
            case RANGE_DESC_TYPE: desc = new RangeTypeDescriptor(); break;
            case SCALAR_ANNOTATION_DESC_TYPE: desc = new ScalarTypeNameAnnotation(); break;
            default:
                if(desc_type >=  TYPE_ANNOTATION_DESC_TYPE_START && desc_type <= TYPE_ANNOTATION_DESC_TYPE_END){
                    desc = new TypeAnnotationDescriptor(desc_type);
                    break;
                }
        }

        try {
            if(desc != null){
                if(!desc.parse(bb)){
                    desc = null;
                }
            }
        }
        catch (Exception e){
            desc = null;
            e.printStackTrace();
        }
        if(desc == null)
            bb.position(bb_pos);

        return desc;
    }

}
