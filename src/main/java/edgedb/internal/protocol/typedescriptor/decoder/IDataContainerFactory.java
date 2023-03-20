package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.typedescriptor.IDataContainer;
import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

public interface IDataContainerFactory<T extends IDataContainer> {
    public T getInstance(TypeDescriptor type_descriptor);
}
