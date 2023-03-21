package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.typedescriptor.TypeDescriptor;

import java.util.UUID;

public interface ITypeDescriptorHolder {

    public TypeDescriptor getInputTypeDescriptor(int index);
    public TypeDescriptor getOutputTypeDescriptor(int index);
    public TypeDescriptor getInputRootTypeDescriptor();
    public TypeDescriptor getOutputRootTypeDescriptor();
    public UUID getInputTypeDescId();
    public UUID getOutputTypeDescId();


}
