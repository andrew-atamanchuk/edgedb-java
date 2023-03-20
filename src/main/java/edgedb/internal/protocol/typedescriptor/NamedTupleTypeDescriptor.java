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

        Map<String, Object> result_map = new HashMap<>();
        for(int i = 0; i < elementCount; i++){
            TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(tupleElements[i].typePos);
            if(parent_desc != null){
                int start_pos_bb = bb.position();
                result_map.put(tupleElements[i].name, parent_desc.decodeData(bb, length));
                length -= (bb.position() - start_pos_bb);
            }
        }

        IDataContainer container = data_factory.getInstance(this);
        container.setData(result_map);
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
