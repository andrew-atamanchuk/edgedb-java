package edgedb.internal.protocol.typedescriptor;

import edgedb.internal.protocol.typedescriptor.decoder.IDataContainerFactory;
import edgedb.internal.protocol.typedescriptor.decoder.ITypeDescriptorHolder;
import edgedb.internal.protocol.utility.UUIDUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class TypeDescriptor implements IDescType {


    protected ITypeDescriptorHolder<TypeDescriptor> descriptor_holder = null;
    protected final byte type;
    protected byte[] id = new byte[Long.SIZE * 2 / Byte.SIZE];
    protected UUID uuid;

    protected IDataContainerFactory data_factory;

    public TypeDescriptor(byte type){
        this.type = type;
    }

    abstract public IDataContainer decodeData(ByteBuffer bb, int length);
    abstract public int encodeData(ByteBuffer bb, IDataContainer container);
    abstract public IDataContainer createInputDataFrame();

    public byte getType(){
        return type;
    }

    public boolean parse(ByteBuffer bb){
        if(bb.remaining() >= id.length) {
            int pos = bb.position();
            bb.get(id, 0, id.length);
            bb.position(pos);
            uuid = UUIDUtils.convertBytesToUUID(bb);
            return true;
        }
        return false;
    }

    public void setDescriptorHolder(ITypeDescriptorHolder holder){
        this.descriptor_holder = holder;
    }

    public void setDataContainerFactory(IDataContainerFactory factory){
        this.data_factory = factory;
    }
}
