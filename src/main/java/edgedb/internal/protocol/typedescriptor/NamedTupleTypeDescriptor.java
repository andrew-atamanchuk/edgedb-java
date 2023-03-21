package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class NamedTupleTypeDescriptor extends TypeDescriptor {
    private short elementCount;
    private TupleElement[] tupleElements;

    public NamedTupleTypeDescriptor(){
        super(NAMED_TUPLE_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining() || elementCount < 0)
            return null;

        int nelems = bb.getInt();
        if(nelems != elementCount){
            bb.position(bb.position() + length - 4);
            System.err.println("ERROR! NamedTupleTypeDescriptor.decodeDate: elementCount (" + elementCount +") in descriptor != elementCount in data (" + nelems + ")");
            return null;
        }

        IDataContainer container = data_factory.getInstance(this);
        for(int i = 0; i < nelems; i++){
            TypeDescriptor parent_desc = descriptor_holder.getOutputTypeDescriptor(tupleElements[i].typePos);
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
            tupleElements = new TupleElement[elementCount];
            for(int i = 0; i < elementCount; i++){
                tupleElements[i] = new TupleElement();
                tupleElements[i].parse(bb);
            }
        }

        return true;
    }
}
