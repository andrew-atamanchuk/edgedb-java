package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.typedescriptor.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class TypeDecoderFactoryImpl implements TypeDecoderFactory, IDataContainerFactory<DataContainerImpl> {

    public static KnownTypeDecoder<BaseScalarType> scalar_type_decoder = new KnownTypeDecoder();


    public boolean decodeDescriptors(CommandDataDescriptor cdd, ITypeDescriptorHolder type_desc_holder){
        try {
            ByteBuffer bb = ByteBuffer.wrap(cdd.getInput_typedesc());
            while(bb.hasRemaining()){
                TypeDescriptor desc = parseTypeDescriptor(bb);
                if(desc != null){
                    desc.setDescriptorHolder(type_desc_holder);
                    desc.setDataContainerFactory(this);
                    type_desc_holder.addInputTypeDescriptor(desc);
                }
            }

            bb = ByteBuffer.wrap(cdd.getOutput_typedesc());
            while(bb.hasRemaining()){
                TypeDescriptor desc = parseTypeDescriptor(bb);
                if(desc != null){
                    desc.setDescriptorHolder(type_desc_holder);
                    desc.setDataContainerFactory(this);
                    type_desc_holder.addOutputTypeDescriptor(desc);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static TypeDescriptor parseTypeDescriptor(ByteBuffer bb) throws TypeNotPresentException {
        TypeDescriptor desc = null;
        int bb_pos = 0;
        try {
            bb_pos = bb.position();
            desc = createInstanceFromBB(bb);
        }
        catch (Exception e){
            bb.position(bb_pos);
            throw  new TypeNotPresentException(String.valueOf(desc != null ? desc.getClass().toString() : "Unknown, type id: " + bb.get()), e);
        }
        finally {
            if(desc == null){
                bb.position(bb_pos);
                throw  new TypeNotPresentException(String.valueOf(desc != null ? desc.getClass().toString() : "Unknown, type id: " + bb.get()), new Exception());
            }
        }

        return desc;
    }

    public static TypeDescriptor createInstanceFromBB(ByteBuffer bb){
        int bb_pos = bb.position();
        byte desc_type = bb.get();
        TypeDescriptor desc = null;
        switch (desc_type){
            case IDescType.SET_DESC_TYPE: desc = new SetTypeDescriptor(); break;
            case IDescType.OBJECT_SHAPE_DESC_TYPE: desc = new ObjectShapeDescriptor(); break;
            case IDescType.BASE_SCALAR_DESC_TYPE: desc = new BaseScalarTypeDescriptor(); break;
            case IDescType.SCALAR_DESC_TYPE: desc = new ScalarTypeDescriptor(); break;
            case IDescType.TUPLE_DESC_TYPE: desc = new TupleTypeDescriptor(); break;
            case IDescType.NAMED_TUPLE_DESC_TYPE: desc = new NamedTupleTypeDescriptor(); break;
            case IDescType.ARRAY_DESC_TYPE: desc = new ArrayTypeDescriptor(); break;
            case IDescType.ENUMERATION_DESC_TYPE: desc = new EnumerationTypeDescriptor(); break;
            case IDescType.INPUT_SHAPE_DESC_TYPE: desc = new InputShapeTypeDescriptor(); break;
            case IDescType.RANGE_DESC_TYPE: desc = new RangeTypeDescriptor(); break;
            case IDescType.SCALAR_ANNOTATION_DESC_TYPE: desc = new ScalarTypeNameAnnotation(); break;
            default:
                if(desc_type >= IDescType.TYPE_ANNOTATION_DESC_TYPE_START && desc_type <= IDescType.TYPE_ANNOTATION_DESC_TYPE_END){
                    desc = new TypeAnnotationDescriptor(desc_type);
                    break;
                }
        }

        try {
            if(desc != null){
                if(!desc.parse(bb)){
                    desc = null;
                }
            }
        }
        catch (Exception e){
            desc = null;
            e.printStackTrace();
        }
        if(desc == null)
            bb.position(bb_pos);

        return desc;
    }

    public static void main(String... args){
        double value = 0.0;
        short weight = 3;
        for (int i = 0; i < 4; i++){
            double multipler =  Math.pow(10, 4 * i);
            value = value + (5 * weight * 10000 / multipler);
        }


        byte[] src_arr = {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, -113, 59, -98, -86, -40, 117, 96, -105, -5, 52, -17, 59, 111, 18, -46, 116, 0, 3, 0, 0, 0, 1, 65, 0, 0, 0, 7, 95, 95, 116, 105, 100, 95, 95, 0, 0, 0, 0, 0, 0, 65, 0, 0, 0, 2, 105, 100, 0, 0, 0, 0, 0, 0, 111, 0, 0, 0, 4, 110, 97, 109, 101, 0, 1};

        TypeDecoderFactoryImpl tdf = new TypeDecoderFactoryImpl();
        ByteBuffer bb = ByteBuffer.wrap(src_arr);
        TypeDescriptor desc = tdf.parseTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
        desc = tdf.parseTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
        desc = tdf.parseTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
    }

    @Override
    public DataContainerImpl getInstance(TypeDescriptor type_descriptor) {
        return new DataContainerImpl(type_descriptor);
    }
}
