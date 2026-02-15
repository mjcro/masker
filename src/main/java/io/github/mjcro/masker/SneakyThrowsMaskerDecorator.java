package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Simple decorator of any other masker that replaces {@link Exception}
 * with {@link RuntimeException} allowing to use maskers in things like lambdas.
 */
public class SneakyThrowsMaskerDecorator<T, R> implements Masker<T, R> {
    private final Masker<T, R> masker;

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
