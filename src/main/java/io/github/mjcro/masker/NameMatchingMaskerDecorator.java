package io.github.mjcro.masker;

import io.github.mjcro.masker.util.ContainsCaseInsensitivePredicate;
import io.github.mjcro.masker.util.EqualsCaseInsensitivePredicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Decorator that applies the underlying masker to a (name, value) pair only when
 * the name matches the configured predicate; otherwise the value is returned unchanged.
 * Typically used by document-level maskers to target specific JSON/XML fields or HTTP headers.
 *
 * @param <T> Type of the value being masked.
 */
public class NameMatchingMaskerDecorator<T> implements Masker<Map.Entry<String, T>, T> {
    private final Predicate<String> namePredicate;
    private final Masker<T, T> masker;

    /**
     * Creates a decorator that triggers on case-insensitive exact name match.
     *
     * @param masker Non-null masker to apply when the name matches.
     * @param names  Non-null list of accepted names.
     * @param <T>    Type of the value being masked.
     * @return New decorator instance.
     */
    public static <T> NameMatchingMaskerDecorator<T> equalsCaseInsensitive(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... names
    ) {
        return new NameMatchingMaskerDecorator<>(
                new EqualsCaseInsensitivePredicate(names),
                masker
        );
    }

    /**
     * Creates a decorator that triggers when the name contains any of the given
     * substrings, checked case-insensitively.
     *
     * @param masker Non-null masker to apply when the name matches.
     * @param names  Non-null list of accepted substrings.
     * @param <T>    Type of the value being masked.
     * @return New decorator instance.
     */
    public static <T> NameMatchingMaskerDecorator<T> containsCaseInsensitive(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... names
    ) {
        return new NameMatchingMaskerDecorator<>(
                new ContainsCaseInsensitivePredicate(names),
                masker
        );
    }

    /**
     * Constructs new decorator.
     *
     * @param namePredicate Non-null predicate evaluated against the entry key.
     * @param masker        Non-null masker applied when the predicate matches.
     */
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

    /**
     * Convenience overload accepting a name and value as separate arguments.
     *
     * @param name  Non-null entry name.
     * @param value Nullable entry value.
     * @return Masked value when the name matches, original value otherwise,
     * or {@code null} when the input value is {@code null}.
     * @throws Exception On any error raised by the underlying masker.
     */
    public @Nullable T applyMasking(@NonNull String name, @Nullable T value) throws Exception {
        return value == null ? null : applyMasking(Map.entry(name, value));
    }
}
