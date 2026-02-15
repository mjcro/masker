package io.github.mjcro.masker;

import io.github.mjcro.masker.util.ContainsCaseInsensitivePredicate;
import io.github.mjcro.masker.util.EqualsCaseInsensitivePredicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class NameMatchingMaskerDecorator<T> implements Masker<Map.Entry<String, T>, T> {
    private final Predicate<String> namePredicate;
    private final Masker<T, T> masker;

    public static <T> NameMatchingMaskerDecorator<T> equalsCaseInsensitive(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... names
    ) {
        return new NameMatchingMaskerDecorator<>(
                new EqualsCaseInsensitivePredicate(names),
                masker
        );
    }

    public static <T> NameMatchingMaskerDecorator<T> containsCaseInsensitive(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... names
    ) {
        return new NameMatchingMaskerDecorator<>(
                new ContainsCaseInsensitivePredicate(names),
                masker
        );
    }

    public NameMatchingMaskerDecorator(@NonNull Predicate<String> namePredicate, @NonNull Masker<T, T> masker) {
        this.namePredicate = Objects.requireNonNull(namePredicate, "namePredicate");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable T applyMasking(Map.@Nullable Entry<String, T> value) throws Exception {
        return value == null
                ? null
                : (namePredicate.test(value.getKey()) ? masker.applyMasking(value.getValue()) : value.getValue());
    }

    public @Nullable T applyMasking(@NonNull String name, @Nullable T value) throws Exception {
        return value == null ? null : applyMasking(Map.entry(name, value));
    }
}
