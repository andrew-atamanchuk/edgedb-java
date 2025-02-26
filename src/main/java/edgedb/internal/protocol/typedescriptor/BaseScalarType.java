package edgedb.internal.protocol.typedescriptor;

public enum BaseScalarType implements IDescType {
    UUID_SCALAR_TYPE,
    STRING_SCALAR_TYPE,
    BYTES_SCALAR_TYPE,
    INT16_SCALAR_TYPE,
    INT32_SCALAR_TYPE,
    INT64_SCALAR_TYPE,
    FLOAT32_SCALAR_TYPE,
    FLOAT64_SCALAR_TYPE,
    DECIMAL_SCALAR_TYPE,
    BOOL_SCALAR_TYPE,
    DATETIME_SCALAR_TYPE,
    DURATION_SCALAR_TYPE,
    RELATIVE_DURATION_SCALAR_TYPE,
    DATE_DURATION_SCALAR_TYPE,
    JSON_SCALAR_TYPE,
    LOCAL_DATE_TIME_SCALAR_TYPE,
    LOCAL_DATE_SCALAR_TYPE,
    LOCAL_TIME_SCALAR_TYPE,
    BIGINT_SCALAR_TYPE,
    MEMORY_SCALAR_TYPE,
}

