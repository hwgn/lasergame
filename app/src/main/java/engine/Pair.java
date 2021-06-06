package engine;

/**
 * Simple tuple type for use in the 2D Maps.
 * https://stackoverflow.com/questions/2670982/using-pairs-or-2-tuples-in-java
 * <p>
 * Note that records are final and therefore don't need a clone() method.
 *
 * @param <T1> Type of first value.
 * @param <T2> Type of second value.
 */
public record Pair<T1, T2>(T1 x, T2 y) {
    /**
     * Returns a new Pair instance.
     *
     * @param x   First value.
     * @param y   Second value.
     * @param <A> Type of first value.
     * @param <B> Type of second value.
     * @return Pair instance of x and y.
     */
    public static <A, B> Pair<A, B> of(A x, B y) {
        return new Pair<>(x, y);
    }
}
