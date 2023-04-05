package edgedb.internal.protocol.typedescriptor;

import edgedb.exceptions.ScalarTypeNotFoundException;
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

        try {
            scalar_type = TypeDecoderFactoryImpl.scalar_type_decoder.decode(id);
        }
        catch (ScalarTypeNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;
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

    @Override
    public IDataContainer createInputDataFrame() {
        return data_factory.getInstance(this);
    }

    private static int encodeBaseScalar(ByteBuffer bb, IDataContainer container, BaseScalarType scalar_type){
        int start_bb_pos = bb.position();
        switch (scalar_type){
            case UUID_SCALAR_TYPE: return UUIDUtils.convertUUIDToBB((UUID)container.getData(), bb);
            case STRING_SCALAR_TYPE: {
                String value = (String) container.getData();
                int value_length = value.length();
                bb.put(value.getBytes(), 0, value_length);
                break;
            }
            case BYTES_SCALAR_TYPE:{
                byte[] arr = (byte[])container.getData();
                bb.put(arr, 0, arr.length);
                break;
            }
            case INT16_SCALAR_TYPE: bb.putShort((short)container.getData()); break;
            case INT32_SCALAR_TYPE: bb.putInt((int)container.getData()); break;
            case INT64_SCALAR_TYPE: bb.putLong((long)container.getData()); break;
            case FLOAT32_SCALAR_TYPE: bb.putFloat((float)container.getData()); break;
            case FLOAT64_SCALAR_TYPE: bb.putDouble((double)container.getData()); break;
            case DECIMAL_SCALAR_TYPE: return -1; //TODO will be implemented
            case BOOL_SCALAR_TYPE: bb.put(((boolean)container.getData()) ? (byte)1 : (byte)0); break;
            case DATETIME_SCALAR_TYPE:
            case LOCAL_DATE_TIME_SCALAR_TYPE:
            case LOCAL_TIME_SCALAR_TYPE:
            case MEMORY_SCALAR_TYPE:
                bb.putLong((long)container.getData()); break;
            case LOCAL_DATE_SCALAR_TYPE: bb.putInt((int)container.getData()); break;
            case DURATION_SCALAR_TYPE:
            case RELATIVE_DURATION_SCALAR_TYPE:
            case DATE_DURATION_SCALAR_TYPE:
                Duration dur = (Duration) container.getData();
                bb.putLong(dur.microseconds);
                bb.putInt(dur.days);
                bb.putInt(dur.months);
                break;
            case BIGINT_SCALAR_TYPE: return -1; //TODO will be implemented
            case JSON_SCALAR_TYPE: {
                bb.put((byte)1); // format is currently always 1, and jsondata is a UTF-8 encoded JSON string.
                String value = (String) container.getData();
                int value_length = value.length();
                bb.put(value.getBytes(), 0, value_length);
                break;
            }
        }

        return bb.position() - start_bb_pos;
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
            case DURATION_SCALAR_TYPE:
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
