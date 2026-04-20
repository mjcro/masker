package io.github.mjcro.masker.util;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Predicate returning {@code true} when the tested string contains at least one of the
 * configured substrings, checked case-insensitively (root locale).
 * {@code null} input always evaluates to {@code false}.
 */
public class ContainsCaseInsensitivePredicate implements Predicate<String> {
    private final Set<String> values;

    /**
     * Constructs predicate with given accepted substrings.
     *
     * @param values Non-null substrings. Lowercased internally.
     */
    public ContainsCaseInsensitivePredicate(String... values) {
        this.values = Arrays.stream(values).map(EqualsCaseInsensitivePredicate::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public boolean test(String s) {
        if (s == null) {
            return false;
        }

        String cis = EqualsCaseInsensitivePredicate.toLowerCase(s);
        for (String value : values) {
            if (cis.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
