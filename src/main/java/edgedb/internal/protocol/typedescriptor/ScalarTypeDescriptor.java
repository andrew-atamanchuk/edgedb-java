package edgedb.internal.protocol.typedescriptor;

import java.nio.ByteBuffer;

public class ScalarTypeDescriptor extends TypeDescriptor {
    private short baseTypePosition;

    public ScalarTypeDescriptor() {
        super(SCALAR_DESC_TYPE);
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        if(length <= 0 || length > bb.remaining())
            return null;

        int start_pos_bb = bb.position();
        TypeDescriptor child_desc = descriptor_holder.getOutputTypeDescriptor(baseTypePosition);
        IDataContainer container = data_factory.getInstance(this);
        if(child_desc != null){
            container.addChild(child_desc.decodeData(bb, length));
        }
        else{
            bb.position(start_pos_bb + length);
        }

        return container;
    }

    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        int bb_start_pos = bb.position();

        if(container.getChildrenIterator().hasNext()) {
            TypeDescriptor child_desc = descriptor_holder.getInputTypeDescriptor(baseTypePosition);
            IDataContainer child_cont = container.getChildrenIterator().next();
            child_desc.encodeData(bb, child_cont);
        }

        return bb.position() - bb_start_pos;
    }

    @Override
    public IDataContainer createInputDataFrame() {
        IDataContainer container = data_factory.getInstance(this);
        TypeDescriptor base_desc = descriptor_holder.getInputTypeDescriptor(baseTypePosition);
        container.addChild(base_desc.createInputDataFrame());
        return container;
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        baseTypePosition = bb.getShort();
        return true;
    }
}

