package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class RangeTypeDescriptor extends TypeDescriptor {
    private short type_pos;

    public RangeTypeDescriptor() {
        super(RANGE_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || bb.remaining() < length)
            return null;

        TypeDescriptor type_desc = descriptor_holder.getTypeDescriptor(type_pos);
        if(type_desc != null) {
            //TODO It needs to be applied correctly.
            byte flags = bb.get();

            Object[] result_range = new Object[2];
            if((RangeFlag.LB_INC.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                result_range[0] = type_desc.decodeData(bb, boundary_length);
            }
            if((RangeFlag.UB_INC.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                result_range[1] = type_desc.decodeData(bb, boundary_length);
            }

            IDataContainer container = data_factory.getInstance(this);
            container.setData(result_range);
            return container;
        }

        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        type_pos = bb.getShort();
        return true;
    }

}
