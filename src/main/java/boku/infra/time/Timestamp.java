package boku.infra.time;

public record Timestamp(long millis) implements Comparable<Timestamp> {
    public static Timestamp zero() {
        return new Timestamp(0);
    }

    @Override
    public int compareTo(Timestamp timestamp) {
        return Long.compare(this.millis, timestamp.millis);
    }
}
