package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Decorator that rewraps any checked {@link Exception} thrown by the underlying masker
 * into an unchecked {@link RuntimeException}, allowing the masker to be used in lambdas,
 * streams and other APIs that do not tolerate checked exceptions.
 *
 * @param <T> Type the wrapped masker accepts.
 * @param <R> Type the wrapped masker returns.
 */
public class SneakyThrowsMaskerDecorator<T, R> implements Masker<T, R> {
    private final Masker<T, R> masker;

    /**
     * Wraps the given masker. Returns the input untouched when it is already a {@code SneakyThrowsMaskerDecorator}.
     *
     * @param masker Non-null masker to wrap.
     * @param <T>    Type the masker accepts.
     * @param <R>    Type the masker returns.
     * @return Decorated masker that never throws checked exceptions.
     */
    public static <T, R> SneakyThrowsMaskerDecorator<T, R> of(@NonNull Masker<T, R> masker) {
        if (masker instanceof SneakyThrowsMaskerDecorator) {
            return (SneakyThrowsMaskerDecorator<T, R>) masker;
        }

        return new SneakyThrowsMaskerDecorator<>(Objects.requireNonNull(masker, "masker"));
    }

    SneakyThrowsMaskerDecorator(@NonNull Masker<T, R> masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable R applyMasking(@Nullable T value) {
        try {
            return masker.applyMasking(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
