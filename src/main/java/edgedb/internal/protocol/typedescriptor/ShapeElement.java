package edgedb.internal.protocol.typedescriptor;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class ShapeElement {
    // Field flags:
    //   1 << 0: the field is implicit
    //   1 << 1: the field is a link property
    //   1 << 2: the field is a link
    private int flags;

    private Cardinality cardinality;

    // Field name.
    private String name;

    // Field type descriptor index.
    private short type_pos;

    public boolean parse(ByteBuffer bb){
        flags = bb.getInt();
        byte cardinality_b = bb.get();
        cardinality = Cardinality.replacementValueOf(cardinality_b);
        int length = bb.getInt();
        name = new String(bb.array(), bb.position(), length);
        bb.position(bb.position() + length);
        type_pos = bb.getShort();
        return true;
    }
}
