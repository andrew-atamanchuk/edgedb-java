package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class EnumerationTypeDescriptor extends TypeDescriptor {
    private short memberCount;
    private String[] members;

    public EnumerationTypeDescriptor(){
        super(ENUMERATION_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        //TODO what needs to be done here?
        return null;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        memberCount = bb.getShort();
        if(memberCount >= 0){
            members = new String[memberCount];
            for(int i = 0; i < memberCount; i++) {
                int length = bb.getInt();
                members[i] = new String(bb.array(), bb.position(), length);
                bb.position(bb.position() + length);
            }
        }

        return true;
    }
}
