package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.typedescriptor.IDescType;
import edgedb.query.IQueryCommand;

import java.util.UUID;

public interface ITypeDescriptorHolder<T extends IDescType> extends IQueryCommand {

    public boolean decodeCommandDataDescriptors(CommandDataDescriptor cdd);
    public void addInputTypeDescriptor(T type_desc);
    public void addOutputTypeDescriptor(T type_desc);
    public T getInputTypeDescriptor(int index);
    public T getOutputTypeDescriptor(int index);
    public T getInputRootTypeDescriptor();
    public T getOutputRootTypeDescriptor();
    public UUID getInputTypeDescId();
    public UUID getOutputTypeDescId();


}
