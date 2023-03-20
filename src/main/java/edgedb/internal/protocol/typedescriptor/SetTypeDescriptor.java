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
    public Object decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        // Number of dimensions, currently must
        // always be 0 or 1. 0 indicates an empty set or array.
        int ndims = bb.getInt();

        int reserved0 = bb.getInt();
        int reserved1 = bb.getInt();

        if(ndims == 0)
            return null;

        int upper = bb.getInt();
        int lower = bb.getInt();

        Object[] result_arr = new Object[upper - lower + 1];
        for(int i = 0; i < result_arr.length; i++){
            int element_length = bb.getInt();
            TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(typePosition);
            if(parent_desc != null){
                result_arr[i] = parent_desc.decodeData(bb, element_length);
            }
            else{
                bb.position(bb.position() + length);
            }
        }

        return result_arr;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        typePosition = bb.getShort();
        return true;
    }
}
