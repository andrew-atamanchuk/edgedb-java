package edgedb.internal.protocol.typedescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DataContainerImpl implements IDataContainer {
    private final TypeDescriptor type_descriptor;
    public ArrayList<IDataContainer> children = new ArrayList<>();
    public Object data;

    public DataContainerImpl(TypeDescriptor type_descriptor){
        this.type_descriptor = type_descriptor;
    }

    @Override
    public byte getType() {
        return type_descriptor.getType();
    }

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
    public Iterator<IDataContainer> getChildrenIterator() {
        return children.iterator();
    }

    @Override
    public int getCountChildren() {
        return children.size();
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public TypeDescriptor getTypeDescriptor() {
        return type_descriptor;
    }


}
