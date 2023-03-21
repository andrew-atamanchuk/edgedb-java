package edgedb.internal.protocol.typedescriptor;

import edgedb.internal.protocol.typedescriptor.decoder.TypeDecoderFactoryImpl;
import edgedb.internal.protocol.utility.UUIDUtils;
import java.util.UUID;
import java.nio.ByteBuffer;

public class BaseScalarTypeDescriptor extends TypeDescriptor {
    // Positive value.
    public static final short POS = 0x0000;
    // Negative value.
    public static final short NEG = 0x4000;

    public BaseScalarType scalar_type;

    public BaseScalarTypeDescriptor(){
        super(BASE_SCALAR_DESC_TYPE);
    }

    @Override
    public boolean parse(ByteBuffer bb) {
        if(!super.parse(bb))
            return false;

        IDescType temp_type = TypeDecoderFactoryImpl.scalar_type_decoder.decode(id);
        if(temp_type instanceof BaseScalarType){
            scalar_type = (BaseScalarType)temp_type;
            return true;
        }

        return false;
    }

    @Override
    public IDataContainer decodeData(ByteBuffer bb, int length) {
        IDataContainer container = data_factory.getInstance(this);
        container.setData(decodeBaseScalar(bb, length, this.scalar_type));
        return container;
    }



    @Override
    public int encodeData(ByteBuffer bb, IDataContainer container) {
        int length = -1;
        try {
            length = encodeBaseScalar(bb, container, scalar_type);
        }
        catch (Exception e){
            length = -1;
            e.printStackTrace();
        }
        return length;
    }

    private static int encodeBaseScalar(ByteBuffer bb, IDataContainer container, BaseScalarType scalar_type){

        switch (scalar_type){
            case UUID_SCALAR_TYPE: return UUIDUtils.convertUUIDToBB((UUID)container.getData(), bb);
            case STRING_SCALAR_TYPE: {
                String value = (String) container.getData();
                int value_length = value.length();
                bb.put(value.getBytes(), 0, value_length);
                return value_length;
            }
        }

        return -1;
    }

    public static Object decodeBaseScalar(ByteBuffer bb, int length, BaseScalarType scalar_type){
        if(length <= 0 || bb.remaining() < length)
            return null;

        switch (scalar_type){
            case UUID_SCALAR_TYPE: return UUIDUtils.convertBytesToUUID(bb);
            case STRING_SCALAR_TYPE: {
                String value = new String(bb.array(), bb.position(), length);
                bb.position(bb.position() + length);
                return value;
            }
            case BYTES_SCALAR_TYPE: {
                byte[] value = new byte[length];
                bb.get(value, 0, length);
                return value;
            }
            case INT16_SCALAR_TYPE: return bb.getShort();
            case INT32_SCALAR_TYPE: return bb.getInt();
            case INT64_SCALAR_TYPE: return bb.getLong();
            case FLOAT32_SCALAR_TYPE: return bb.getFloat();
            case FLOAT64_SCALAR_TYPE: return bb.getDouble();
            case DECIMAL_SCALAR_TYPE: {
                //TODO This is done for the sake of the logic example. Will need to be replaced with a special class that stores the decimal
                short ndigits = bb.getShort();
                short weight = bb.getShort();
                short sign = bb.getShort();
                short dscale = bb.getShort(); // a number of simbols after comma
                double value = 0.0;
                for (int i = 0; i < ndigits; i++){
                    short digit = bb.getShort();
                    double multipler =  Math.pow(10, 4 * i);
                    value = value + (digit * weight * 10000 / multipler);
                }
                if(sign == NEG)
                    value = -value;
                return value;
            }
            case BOOL_SCALAR_TYPE: return bb.get() == 1;
            case DATETIME_SCALAR_TYPE:
            case LOCAL_DATE_TIME_SCALAR_TYPE:
            case LOCAL_TIME_SCALAR_TYPE:
            case MEMORY_SCALAR_TYPE:
                return bb.getLong();
            case LOCAL_DATE_SCALAR_TYPE: return bb.getInt();
            case DURATION_SCALAR_TYPE: // TODO this can be changed to Long type
            case RELATIVE_DURATION_SCALAR_TYPE:
            case DATE_DURATION_SCALAR_TYPE:
                return new Duration(bb.getLong(), bb.getInt(), bb.getInt());
            case BIGINT_SCALAR_TYPE: {
                //TODO This is done for the sake of the logic example. Will need to be replaced with a special class that stores the big integer
                short ndigits = bb.getShort();
                short weight = bb.getShort();
                short sign = bb.getShort();
                short dscale = bb.getShort(); // a number of simbols after comma
                long value = 0;
                for (int i = 0; i < ndigits; i++) {
                    short digit = bb.getShort();
                    long multipler = (long) Math.pow(10, 4 * i);
                    value = value + (digit * weight * 10000 / multipler);
                }
                if (sign == NEG)
                    value = -value;
                return value;
            }
            case JSON_SCALAR_TYPE: { //TODO will need to be checked: does the length include format byte?
                byte format = bb.get();
                String value = new String(bb.array(), bb.position(), length - 1);
                bb.position(bb.position() + length - 1);
                return value;
            }

        }

        return null;
    }
}
