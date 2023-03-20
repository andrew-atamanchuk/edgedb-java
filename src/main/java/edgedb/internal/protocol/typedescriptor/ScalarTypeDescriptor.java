package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class ScalarTypeDescriptor extends TypeDescriptor {
    private short baseTypePosition;

    public ScalarTypeDescriptor() {
        super(SCALAR_DESC_TYPE);
    }

    @Override
    public Object decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        TypeDescriptor parent_desc = descriptor_holder.getTypeDescriptor(baseTypePosition);
        if(parent_desc != null){
            return parent_desc.decodeData(bb, length);
        }

        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        baseTypePosition = bb.getShort();
        return true;
    }
}

