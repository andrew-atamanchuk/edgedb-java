package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class ArrayTypeDescriptor extends TypeDescriptor {
    private short type_pos;
    private short dimensionCount;
    private int[] dimensions;

    public ArrayTypeDescriptor(){
        super(ARRAY_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(type_pos);
        if(parent_desc != null) {
            //TODO what needs to be done here?
        }

        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        type_pos = bb.getShort();
        dimensionCount = bb.getShort();
        if(dimensionCount >= 0) {
            dimensions = new int[dimensionCount];
            for (int i = 0; i < dimensionCount; i++) {
                dimensions[i] = bb.getInt();
            }
        }

        return true;
    }
}
