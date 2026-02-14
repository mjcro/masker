package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Masker is a component responsible for sensitive information masking.
 *
 * @param <T> Type masker works with.
 * @param <R> Type masker returns.
 */
@FunctionalInterface
public interface Masker<T, R> {
    /**
     * Constructs masker instance from given function.
     *
     * @param func Source function.
     * @return Masker.
     */
    static <T, R> Masker<T, R> from(@NonNull Function<T, R> func) {
        Objects.requireNonNull(func, "func");
        return func::apply;
    }

    /**
     * Performs masking of given value.
     *
     * @param value Value to mask.
     * @return Masked value.
     * @throws Exception On any error.
     */
    @Nullable R applyMasking(@Nullable T value) throws Exception;

    /**
     * Constructs and returns composed masker.
     *
     * @param next Next masker to chain.
     * @return Masker.
     */
    default <R2> @NonNull Masker<T, R2> andThen(@NonNull Masker<R, R2> next) {
        Objects.requireNonNull(next, "next");
        return t -> next.applyMasking(applyMasking(t));
    }
}
