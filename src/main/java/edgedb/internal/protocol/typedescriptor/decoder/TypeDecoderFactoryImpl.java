package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.internal.protocol.typedescriptor.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TypeDecoderFactoryImpl implements TypeDecoderFactory, ITypeDescriptorHolder, IDataContainerFactory<DataContainerImpl> {

    public static KnownTypeDecoder scalar_type_decoder = new KnownTypeDecoder();

    private ArrayList<TypeDescriptor> desc_list = new ArrayList<>();
    private ObjectShapeDescriptor root_osd = null;

    @Override
    public TypeDescriptor getRootTypeDescriptor() {
        return root_osd;
    }

    @Override
    public TypeDescriptor getTypeDescriptor(int index) {
        if(index < 0 || desc_list.size() <= index)
            return null;

        return desc_list.get(index);
    }

    @Override
    public boolean decodeDescriptors(ByteBuffer bb) {
        while(bb.hasRemaining()){
            TypeDescriptor desc = getTypeDescriptor(bb);
            if(desc != null){
                desc.setDescriptorHolder(this);
                desc.setDataContainerFactory(this);
                desc_list.add(desc_list.size(), desc);
                if(desc instanceof ObjectShapeDescriptor)
                    root_osd = (ObjectShapeDescriptor)desc;
            }
        }

        return true;
    }

    @Override
    public TypeDescriptor getTypeDescriptor(ByteBuffer bb) throws TypeNotPresentException {
        TypeDescriptor desc = null;
        int bb_pos = 0;
        try {
            bb_pos = bb.position();
            desc = TypeDescriptor.createInstanceFromBB(bb);
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
        TypeDescriptor desc = tdf.getTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
        desc = tdf.getTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
        desc = tdf.getTypeDescriptor(bb);
        if(desc != null){
            System.out.println("Desc parsed succes: " + desc.getClass());
        }
    }

    @Override
    public DataContainerImpl getInstance(TypeDescriptor type_descriptor) {
        return new DataContainerImpl(type_descriptor);
    }
}
