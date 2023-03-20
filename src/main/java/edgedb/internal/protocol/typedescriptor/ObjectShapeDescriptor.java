package edgedb.internal.protocol.typedescriptor;

import lombok.Data;

import java.nio.ByteBuffer;

public class ObjectShapeDescriptor extends TypeDescriptor {
    public short elementCount;
    public ShapeElement[] shapeElements;

    public ObjectShapeDescriptor(){
        super(OBJECT_SHAPE_DESC_TYPE);
    }

    @Override
    public Object decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || bb.remaining() < 4 || bb.remaining() < length)
            return null;

        int count_obj = bb.getInt();

        if(count_obj <= 0)
            return null;

        if(count_obj != elementCount){
            System.out.println("ERROR! the number of elements in data response does not match the number of ShapeElements ");
            return null;
        }

        Object[] decoded_arr = new Object[count_obj];

        for(int i = 0; i < count_obj; i++){
            ShapeElement shape_elem = shapeElements[i];
            TypeDescriptor curr_desc = descriptor_holder.getTypeDescriptor(shape_elem.getType_pos());
            Object data_object = null;
            try {
                int reserved = bb.getInt();
                int element_length = bb.getInt();
                data_object = curr_desc.decodeData(bb, element_length);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            decoded_arr[i] = data_object;
        }

        return decoded_arr;
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
