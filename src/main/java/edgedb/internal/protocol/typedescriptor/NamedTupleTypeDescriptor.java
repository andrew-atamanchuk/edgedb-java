package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
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
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        if(elementCount != tupleElements.length) {
            System.err.println("NamedTupleTypeDescriptor.encodeData: elementCount (" + elementCount +") not equal shapeElements length (" + tupleElements.length + ")");
            return -1;
        }

        int start_bb_position = bb.position();
        bb.putInt(elementCount);

        Iterator<IDataContainer> child_iter = container.getChildrenIterator();
        for(int i = 0; i < tupleElements.length && child_iter.hasNext(); i++){
            TupleElement element = tupleElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getInputTypeDescriptor(element.typePos);
            if(curr_desc == null){
                System.err.println("NamedTupleTypeDescriptor.encodeData: TypeDescriptor (" + element.typePos +") is null!");
                return -1;
            }
            IDataContainer child_container = child_iter.next();
            bb.putInt(0); //reserved
            int bb_start_pos_element = bb.position();
            bb.putInt(0); //length of element
            int child_length = curr_desc.encodeData(bb, child_container);
            bb.putInt(bb_start_pos_element, child_length);
        }

        return bb.position() - start_bb_position;
    }

    @Override
    public IDataContainer createInputDataFrame() {
        IDataContainer container = data_factory.getInstance(this);

        for(int i = 0; i < elementCount; i++){
            TupleElement tuple_elem = tupleElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getInputTypeDescriptor(tuple_elem.typePos);
            IDataContainer child_container = curr_desc.createInputDataFrame();
            container.addChild(child_container);
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
