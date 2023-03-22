package edgedb.internal.protocol.typedescriptor;

public class Duration {
    public long microseconds;
    // deprecated, is always 0
    public int days;
    // deprecated, is always 0
    public int months;

    public Duration(long microseconds, int days, int months){
        this.microseconds = microseconds;
        this.days = days;
        this.months = months;
    }

}
