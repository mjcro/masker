package io.github.mjcro.masker.util;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Predicate returning {@code true} when the tested string equals one of the configured
 * values, checked case-insensitively (root locale).
 * {@code null} input always evaluates to {@code false}.
 */
public class EqualsCaseInsensitivePredicate implements Predicate<String> {
    private final Set<String> values;

    /**
     * Lowercases the given string using {@link Locale#ROOT}, avoiding locale-specific surprises
     * like the Turkish dotted-I.
     *
     * @param s Non-null source string.
     * @return Non-null lowercase form.
     */
    public static @NonNull String toLowerCase(@NonNull String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Constructs predicate with given accepted values.
     *
     * @param values Non-null values. Lowercased internally.
     */
    public EqualsCaseInsensitivePredicate(String... values) {
        this.values = Arrays.stream(values).map(EqualsCaseInsensitivePredicate::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public boolean test(final String s) {
        return s != null && values.contains(toLowerCase(s));
    }
}
