package edgedb.internal.protocol.typedescriptor;

import lombok.Data;

import java.nio.ByteBuffer;

public class TupleTypeDescriptor extends TypeDescriptor {
    private short elementCount;
    private short[] elementTypes;

    public TupleTypeDescriptor(){
        super(TUPLE_DESC_TYPE);
    }

    @Override
    public Object decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining() || elementCount < 0)
            return null;

        Object[] result_arr = new Object[elementCount];
        for(int i = 0; i < elementCount; i++){
            TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(elementTypes[i]);
            if(parent_desc != null){
                int start_pos_bb = bb.position();
                result_arr[i] = parent_desc.decodeData(bb, length);
                length -= (bb.position() - start_pos_bb);
            }
        }

        return result_arr;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        elementCount = bb.getShort();
        if(elementCount >= 0){
            elementTypes = new short[elementCount];
            for(int i = 0; i < elementCount; i++){
                elementTypes[i] = bb.getShort();
            }
        }
        return true;
    }
}
