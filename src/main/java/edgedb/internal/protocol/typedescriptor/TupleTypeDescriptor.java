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

        int nelems = bb.getInt();
        if(nelems != elementCount){
            bb.position(bb.position() + length - 4);
            System.err.println("ERROR! TupleTypeDescriptor.decodeDate: elementCount (" + elementCount +") in descriptor != elementCount in data (" + nelems + ")");
            return null;
        }

        IDataContainer container = data_factory.getInstance(this);
        for(int i = 0; i < nelems; i++){
            TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(elementTypes[i]);
            if(parent_desc != null){
                int reserved = bb.getInt();
                int elem_length = bb.getInt();
                container.addChild(parent_desc.decodeData(bb, elem_length));
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
