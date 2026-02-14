package io.github.mjcro.masker.util;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContainsCaseInsensitivePredicate implements Predicate<String> {
    private final Set<String> values;

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
