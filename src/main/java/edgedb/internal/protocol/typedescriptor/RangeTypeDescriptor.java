package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class RangeTypeDescriptor extends TypeDescriptor {
    private short type_pos;

    public RangeTypeDescriptor() {
        super(RANGE_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || bb.remaining() < length)
            return null;

        TypeDescriptor type_desc = descriptor_holder.getOutputTypeDescriptor(type_pos);
        if(type_desc != null) {
            IDataContainer container = data_factory.getInstance(this);
            byte flags = bb.get();

            //TODO temporary: possible server error
            if((RangeFlag.LB_INC.flag & flags) > 0 || (RangeFlag.UB_INC.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
                boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
            }
/*
            if((RangeFlag.LB_INC.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
            }
            if((RangeFlag.UB_INC.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
            }
*/
            if((RangeFlag.EMPTY.flag & flags) > 0){

            }
            if((RangeFlag.LB_INF.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
            }
            if((RangeFlag.UB_INF.flag & flags) > 0){
                int boundary_length  = bb.getInt();
                container.addChild(type_desc.decodeData(bb, boundary_length));
            }

            return container;
        }

        return null;
    }

    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        //TODO temporary: not yet defined how to get the flag
        Iterator<IDataContainer> iter = container.getChildrenIterator();
        int index = 0;
        byte flag = 0;

        int start_bb_pos = bb.position();
        bb.put(RangeFlag.EMPTY.flag);
        while (iter.hasNext()){
            IDataContainer child_cont = iter.next();
            if(child_cont == null) {
                if (index == 0)
                    flag |= RangeFlag.LB_INF.flag;
                else
                    flag |= RangeFlag.UB_INF.flag;
            }
            else{
                if(child_cont.getData() != null){
                    if(index == 0)
                        flag |= RangeFlag.LB_INC.flag;
                    else
                        flag |= RangeFlag.UB_INC.flag;
                }
                int start_elem_pos = bb.position();
                bb.putInt(0);
                TypeDescriptor type_desc = descriptor_holder.getInputTypeDescriptor(type_pos);
                int child_length = type_desc.encodeData(bb, child_cont);
                bb.putInt(start_elem_pos, child_length);
            }
        }

        bb.put(start_bb_pos, flag);
        return bb.position() - start_bb_pos;
    }

    @Override
    public IDataContainer createInputDataFrame() {
        IDataContainer container = data_factory.getInstance(this);
        TypeDescriptor type_desc = descriptor_holder.getInputTypeDescriptor(type_pos);
        if(type_desc != null) {
            //TODO temporary: possible server error
            container.addChild(type_desc.createInputDataFrame());
            container.addChild(type_desc.createInputDataFrame());
        }

        return container;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        type_pos = bb.getShort();
        return true;
    }

}
