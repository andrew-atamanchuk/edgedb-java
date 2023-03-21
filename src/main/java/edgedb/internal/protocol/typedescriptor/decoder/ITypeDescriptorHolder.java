package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

public interface ITypeDescriptorHolder {

    public TypeDescriptor getRootTypeDescriptor();
    public TypeDescriptor getTypeDescriptor(int index);
}
