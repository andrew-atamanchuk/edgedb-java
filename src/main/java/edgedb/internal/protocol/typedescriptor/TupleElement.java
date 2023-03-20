package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class TupleElement {

    public String name;

    // Make sure this is int16
    public short typePos;

    public boolean parse(ByteBuffer bb){
        int length = bb.getInt();
        if(length >= 0){
            name = new String(bb.array(), bb.position(), length);
            bb.position(bb.position() + length);
        }

        typePos = bb.getShort();
        return true;
    }
}
