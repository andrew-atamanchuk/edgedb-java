package edgedb.internal.protocol.typedescriptor;


import java.nio.ByteBuffer;

// SetType is encoded as a single-dimensional array
public class SetTypeDescriptor extends TypeDescriptor {

    // Set element type descriptor index.
    private short typePosition;

    public SetTypeDescriptor(){
        super(SET_DESC_TYPE);
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

        TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(typePosition);
        if(parent_desc == null) {
            System.err.println("Error! Type descriptor not found at index: " + typePosition);
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

        typePosition = bb.getShort();
        return true;
    }
}
