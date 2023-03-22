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

        IDataContainer container = data_factory.getInstance(this);
        String value = new String(bb.array(), bb.position(), length);
        bb.position(bb.position() + length);
        container.setData(value);

        return container;
    }

    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        Object data = container.getData();
        int start_bb_pos = bb.position();

        String str = data.toString();
        bb.put(str.getBytes(), 0, str.length());

        return bb.position() - start_bb_pos;
    }

    @Override
    public IDataContainer createInputDataFrame() {
        return data_factory.getInstance(this);
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
