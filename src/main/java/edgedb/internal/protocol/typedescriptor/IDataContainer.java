package edgedb.internal.protocol.typedescriptor;

public interface IDataContainer {
    public byte getType();
    public BaseScalarType getScalarType();
    public void addChild(IDataContainer child);
    public void setData(Object data);
    public IDataContainer getInstance(TypeDescriptor type_descriptor);
    public TypeDescriptor getTypeDescriptor();
}
