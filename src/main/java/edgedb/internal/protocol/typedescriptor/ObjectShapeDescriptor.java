package edgedb.internal.protocol.typedescriptor;

import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class ObjectShapeDescriptor extends TypeDescriptor {
    public short elementCount;
    public ShapeElement[] shapeElements;

    public ObjectShapeDescriptor(){
        super(OBJECT_SHAPE_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || bb.remaining() < 4 || bb.remaining() < length)
            return null;

        int count_obj = bb.getInt();

        if(count_obj <= 0)
            return null;

        if(count_obj != elementCount){
            System.out.println("ERROR! the number of elements in data response does not match the number of ShapeElements ");
            return null;
        }

        IDataContainer container = data_factory.getInstance(this);

        for(int i = 0; i < count_obj; i++){
            ShapeElement shape_elem = shapeElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getOutputTypeDescriptor(shape_elem.getType_pos());
            IDataContainer data_object = null;
            try {
                int reserved = bb.getInt();
                int element_length = bb.getInt();
                data_object = curr_desc.decodeData(bb, element_length);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            container.addChild(data_object);
        }

        return container;
    }

    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        if(elementCount != shapeElements.length) {
            System.err.println("ObjectShapeDescriptor.encodeData: elementCount (" + elementCount +") not equal shapeElements length (" + shapeElements.length + ")");
            return -1;
        }

        int start_bb_position = bb.position();
        bb.putInt(elementCount);

        Iterator<IDataContainer> child_iter = container.getChildrenIterator();
        for(int i = 0; i < shapeElements.length && child_iter.hasNext(); i++){
            ShapeElement element = shapeElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getInputTypeDescriptor(element.getType_pos());
            if(curr_desc == null){
                System.err.println("ObjectShapeDescriptor.encodeData: TypeDescriptor (" + element.getType_pos() +") is null!");
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
            ShapeElement shape_elem = shapeElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getInputTypeDescriptor(shape_elem.getType_pos());
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
        if(elementCount >= 0) {
            shapeElements = new ShapeElement[elementCount];
            for (int i = 0; i < elementCount; i++) {
                shapeElements[i] = new ShapeElement();
                shapeElements[i].parse(bb);
            }
        }

        return true;
    }
}
