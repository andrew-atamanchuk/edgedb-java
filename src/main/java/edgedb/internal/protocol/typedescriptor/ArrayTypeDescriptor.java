package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class ArrayTypeDescriptor extends TypeDescriptor {
    private short type_pos;
    private short dimensionCount;

    // Sizes of array dimensions, -1 indicates
    // unbound dimension.
    private int[] dimensions;

    public ArrayTypeDescriptor(){
        super(ARRAY_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        int start_pos_bb = bb.position();
        // Number of dimensions, currently must
        // always be 0 or 1. 0 indicates an empty set or array.
        int ndims = bb.getInt();

        int reserved0 = bb.getInt();
        int reserved1 = bb.getInt();

        if(ndims == 0)
            return null;

        TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(type_pos);
        if(parent_desc == null) {
            System.err.println("Error! Type descriptor not found at index: " + type_pos);
            bb.position(start_pos_bb + length);
            return null;
        }

        int upper = bb.getInt();
        int lower = bb.getInt();

        IDataContainer container = data_factory.getInstance(this);
        int result_length = upper - lower + 1;
        for(int i = 0; i < result_length; i++){
            int element_length = bb.getInt();
            container.addChild(parent_desc.decodeData(bb, element_length));

        }

        return container;
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
