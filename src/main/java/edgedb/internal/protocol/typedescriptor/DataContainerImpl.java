package edgedb.internal.protocol.typedescriptor;

import java.util.HashSet;
import java.util.Set;

public class DataContainerImpl implements IDataContainer {
    private final TypeDescriptor type_descriptor;
    public Set<IDataContainer> children = new HashSet<>();
    public Object data;

    public DataContainerImpl(TypeDescriptor type_descriptor){
        this.type_descriptor = type_descriptor;
    }

    @Override
    public byte getType() {
        return type_descriptor.getType();
    }

    @Override
    public BaseScalarType getScalarType() {
        if(getType() == TypeDescriptor.BASE_SCALAR_DESC_TYPE){
            return ((BaseScalarTypeDescriptor)type_descriptor).scalar_type;
        }

        return null;
    }

    @Override
    public void addChild(IDataContainer child) {
        children.add(child);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public IDataContainer getInstance(TypeDescriptor type_descriptor) {
        return new DataContainerImpl(type_descriptor);
    }

    @Override
    public TypeDescriptor getTypeDescriptor() {
        return type_descriptor;
    }


}
