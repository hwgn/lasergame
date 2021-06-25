package engine;

/**
 * Simple tuple type for use in the 2D Maps.
 * <p>
 * Note that records are final and therefore don't need a clone() method.
 * Note also that The {@link #equals(Object)} method is already implemented for records.
 *
 * @param <T> Type of first value.
 * @param <U> Type of second value.
 */
public record Pair<T, U>(T x, U y) {
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
