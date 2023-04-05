package edgedb.internal.protocol;

import edgedb.internal.protocol.typedescriptor.ObjectShapeDescriptor;
import edgedb.internal.protocol.typedescriptor.TypeDescriptor;
import edgedb.internal.protocol.typedescriptor.decoder.ITypeDescriptorHolder;
import edgedb.internal.protocol.typedescriptor.decoder.TypeDecoderFactory;
import edgedb.internal.protocol.typedescriptor.decoder.TypeDecoderFactoryImpl;

import java.util.ArrayList;
import java.util.UUID;

public class SuperQuery implements ITypeDescriptorHolder<TypeDescriptor> {

    private static TypeDecoderFactory decode_factory = new TypeDecoderFactoryImpl();
    private CommandDataDescriptor data_descriptor;

    private ArrayList<TypeDescriptor> input_desc_list = new ArrayList<>();
    private ArrayList<TypeDescriptor> output_desc_list = new ArrayList<>();

    private ObjectShapeDescriptor output_root_osd = null;
    private ObjectShapeDescriptor input_root_osd = null;

    public String command;

    public char output_format;
    public char cardinality;
    @Override
    public TypeDescriptor getInputRootTypeDescriptor() {
        return input_root_osd;
    }

    @Override
    public TypeDescriptor getOutputRootTypeDescriptor() {
        return output_root_osd;
    }

    @Override
    public boolean decodeCommandDataDescriptors(CommandDataDescriptor cdd) {
        if(cdd == null)
            return false;
        this.data_descriptor = cdd;
        return decode_factory.decodeDescriptors(cdd, this);
    }

    @Override
    public void addInputTypeDescriptor(TypeDescriptor type_desc) {
        input_desc_list.add(type_desc);
        if(type_desc instanceof ObjectShapeDescriptor)
            input_root_osd = (ObjectShapeDescriptor)type_desc;
    }

    @Override
    public void addOutputTypeDescriptor(TypeDescriptor type_desc) {
        output_desc_list.add(type_desc);
        if(type_desc instanceof ObjectShapeDescriptor)
            output_root_osd = (ObjectShapeDescriptor)type_desc;
    }


    @Override
    public TypeDescriptor getInputTypeDescriptor(int index) {
        if(index < 0 || input_desc_list.size() <= index)
            return null;

        return input_desc_list.get(index);
    }

    @Override
    public TypeDescriptor getOutputTypeDescriptor(int index) {
        if(index < 0 || output_desc_list.size() <= index)
            return null;

        return output_desc_list.get(index);
    }

    @Override
    public UUID getInputTypeDescId(){
        return data_descriptor.input_typedesc_id;
    }

    @Override
    public UUID getOutputTypeDescId(){
        return data_descriptor.output_typedesc_id;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    public char outputFormat() {
        return output_format;
    }

    @Override
    public char cardinality() {
        return cardinality;
    }

}
