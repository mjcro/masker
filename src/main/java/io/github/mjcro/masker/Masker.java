package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Strategy for redacting sensitive information from a value.
 *
 * <p>A {@code Masker} is a single-method functional interface that transforms an
 * input of type {@code T} into a masked output of type {@code R}. Implementations
 * are expected to be referentially transparent and safe for concurrent use unless
 * explicitly documented otherwise.
 *
 * <p>Implementations should preserve <em>reference equality</em> when they perform
 * no change: returning the very same instance signals downstream composers
 * (for example {@link SequentialMasker} or document-level maskers) that no
 * transformation was applied and enables them to short-circuit.
 *
 * <p>{@code null} is a valid input. Implementations are encouraged to propagate
 * {@code null} rather than throw {@link NullPointerException}.
 *
 * @param <T> input value type.
 * @param <R> masked value type.
 * @since 0.0.1
 */
@FunctionalInterface
public interface Masker<T, R> {
    /**
     * Adapts a plain {@link Function} to the {@code Masker} contract.
     *
     * <p>Useful when an existing transformation needs to participate in a masker
     * pipeline without declaring checked exceptions.
     *
     * @param func backing function; must not be {@code null}.
     * @param <T>  input value type.
     * @param <R>  masked value type.
     * @return masker delegating every call to {@code func}.
     * @throws NullPointerException if {@code func} is {@code null}.
     */
    static <T, R> Masker<T, R> from(@NonNull Function<T, R> func) {
        Objects.requireNonNull(func, "func");
        return func::apply;
    }

    /**
     * Applies the masking strategy to the supplied value.
     *
     * @param value value to mask; may be {@code null}.
     * @return masked value; may be {@code null}.
     * @throws Exception if the underlying implementation fails to produce a
     *                   masked value (for example, due to I/O or parsing errors
     *                   in document-level maskers).
     */
    @Nullable R applyMasking(@Nullable T value) throws Exception;

    /**
     * Returns a composed masker that first applies this masker to its input and
     * then applies {@code next} to the result.
     *
     * <p>If either stage throws an exception, it is propagated to the caller of
     * the composed masker.
     *
     * @param next  masker to apply after this one; must not be {@code null}.
     * @param <R2>  output type of the composed masker.
     * @return composed masker; never {@code null}.
     * @throws NullPointerException if {@code next} is {@code null}.
     */
    default <R2> @NonNull Masker<T, R2> andThen(@NonNull Masker<R, R2> next) {
        Objects.requireNonNull(next, "next");
        return t -> next.applyMasking(applyMasking(t));
    }
}
