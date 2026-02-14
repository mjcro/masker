package io.github.mjcro.masker.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EqualsCaseInsensitivePredicate implements Predicate<String> {
    private final Set<String> values;

    static String toLowerCase(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    public EqualsCaseInsensitivePredicate(String... values) {
        this.values = Arrays.stream(values).map(EqualsCaseInsensitivePredicate::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public boolean test(final String s) {
        return s != null && values.contains(toLowerCase(s));
    }
}
