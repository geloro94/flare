package de.medizininformatikinitiative.flare;

import java.util.List;
import java.util.stream.Stream;

public interface Util {

    static <T, U extends T> List<T> add(List<T> xs, U x) {
        return Stream.concat(xs.stream(), Stream.of(x)).toList();
    }

    /**
     * In Clojure this is:
     * <pre>
     * (reduce
     *   (fn [result rows]
     *     (reduce
     *       (fn [new-result result-rows]
     *         (reduce
     *           (fn [new-result-row x]
     *             (conj new-result-row (conj result-rows x)))
     *           new-result
     *           rows))
     *       []
     *       result))
     *   [[]]
     *   matrix)
     * </pre>
     */
    static <T> List<List<T>> cartesianProduct(List<List<T>> matrix) {
        var result = List.of(List.<T>of());
        for (var rows : matrix) {
            result = cartesianProductHelper1(result, rows);
        }
        return result;
    }

    private static <T> List<List<T>> cartesianProductHelper1(List<List<T>> result, List<T> rows) {
        var newResult = List.<List<T>>of();
        for (var resultRows : result) {
            newResult = cartesianProductHelper2(newResult, resultRows, rows);
        }
        return newResult;
    }

    private static <T> List<List<T>> cartesianProductHelper2(List<List<T>> newResult, List<T> resultRows, List<T> rows) {
        for (var x : rows) {
            newResult = add(newResult, add(resultRows, x));
        }
        return newResult;
    }

    static <T> List<T> concat(List<T> as, List<? extends T> bs) {
        return Stream.concat(as.stream(), bs.stream()).toList();
    }

    static double durationSecondsSince(long startNanoTime) {
        return ((double) (System.nanoTime() - startNanoTime)) / 1e9;
    }
}
