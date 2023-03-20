package edgedb.internal.protocol.typedescriptor;

import java.util.Iterator;

public interface IDataContainer {
    public byte getType();
    public void addChild(IDataContainer child);
    public Iterator<IDataContainer> getChildrenIterator();
    public int getCountChildren();
    public void setData(Object data);
    public Object getData();
    public TypeDescriptor getTypeDescriptor();
}
