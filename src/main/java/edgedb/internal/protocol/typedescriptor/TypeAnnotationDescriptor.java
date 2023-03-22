package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class TypeAnnotationDescriptor extends TypeDescriptor {
    //type of descriptor in range 0x80..0xfe from 128-254

    private String annotation;

    public TypeAnnotationDescriptor(byte type){
        super(type);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        return null;
    }

    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        return 0;
    }

    @Override
    public IDataContainer createInputDataFrame() {
        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        int length = bb.getInt();
        if(length >= 0){
            annotation = new String(bb.array(), bb.position(), length);
            bb.position(bb.position() + length);
        }
        return true;
    }
}
