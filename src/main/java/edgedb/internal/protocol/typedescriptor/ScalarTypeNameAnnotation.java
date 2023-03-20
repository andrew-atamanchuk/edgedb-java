package edgedb.internal.protocol.typedescriptor;


import lombok.Data;

import java.nio.ByteBuffer;

public class ScalarTypeNameAnnotation extends TypeDescriptor {
    private String typeName;

    public ScalarTypeNameAnnotation(){
        super(SCALAR_ANNOTATION_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        int length = bb.getInt();
        if(length >= 0){
            typeName = new String(bb.array(), bb.position(), length);
            bb.position(bb.position() + length);
        }
        return true;
    }
}
