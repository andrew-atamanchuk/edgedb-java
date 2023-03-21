package edgedb.internal.protocol;

public enum CompilationFlag {
    INJECT_OUTPUT_TYPE_IDS(0x1),
    INJECT_OUTPUT_TYPE_NAMES(0x2),
    INJECT_OUTPUT_OBJECT_IDS(0x4);

    long value;

    CompilationFlag(long value){
        this.value = value;
    }
}
