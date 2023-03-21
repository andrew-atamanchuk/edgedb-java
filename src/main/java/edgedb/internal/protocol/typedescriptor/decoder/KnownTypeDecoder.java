package edgedb.internal.protocol.typedescriptor.decoder;

import edgedb.exceptions.ScalarTypeNotFoundException;
import edgedb.internal.protocol.typedescriptor.*;

import java.util.Arrays;

public final class KnownTypeDecoder<T extends IDescType> {

    public final T decode(byte[] value) throws ScalarTypeNotFoundException {

        if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0})) {
            return (T) BaseScalarType.UUID_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1})) {
            return (T) BaseScalarType.STRING_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2})) {
            return (T) BaseScalarType.BYTES_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3})) {
            return (T) BaseScalarType.INT16_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4})) {
            return (T) BaseScalarType.INT32_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 5})) {
            return (T) BaseScalarType.INT64_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 6})) {
            return (T) BaseScalarType.FLOAT32_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 7})) {
            return (T) BaseScalarType.FLOAT64_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8})) {
            return (T) BaseScalarType.DECIMAL_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 9})) {
            return (T) BaseScalarType.BOOL_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 10})) {
            return (T) BaseScalarType.DATETIME_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 11})) {
            return (T) BaseScalarType.LOCAL_DATE_TIME_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 12})) {
            return (T) BaseScalarType.LOCAL_DATE_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 13})) {
            return (T) BaseScalarType.LOCAL_TIME_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 14})) {
            return (T) BaseScalarType.DURATION_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 15})) {
            return (T) BaseScalarType.JSON_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 16})) {
            return (T) BaseScalarType.BIGINT_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17})) {
            return (T) BaseScalarType.RELATIVE_DURATION_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 18})) {
            return (T) BaseScalarType.DATE_DURATION_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 48})) {
            return (T) BaseScalarType.MEMORY_SCALAR_TYPE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1})) {
            throw new ScalarTypeNotFoundException();
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1})) {
            return (T) TupleType.ANY_TUPLE;
        } else if (Arrays.equals(value, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2})) {
            return (T) TupleType.ANY_TUPLE;
        } else {
            throw new ScalarTypeNotFoundException();
        }
    }
}


