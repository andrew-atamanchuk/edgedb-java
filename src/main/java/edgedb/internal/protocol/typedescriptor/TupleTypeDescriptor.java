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
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining() || elementCount < 0)
            return null;

        IDataContainer container = data_factory.getInstance(this);
        for(int i = 0; i < elementCount; i++){
            TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(elementTypes[i]);
            if(parent_desc != null){
                int start_pos_bb = bb.position();
                container.addChild(parent_desc.decodeData(bb, length));
                length -= (bb.position() - start_pos_bb);
            }
        }

        return container;
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
