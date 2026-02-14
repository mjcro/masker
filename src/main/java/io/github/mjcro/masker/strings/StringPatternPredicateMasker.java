package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Decorator over other masker that invokes it only if regex predicate matches.
 */
public class StringPatternPredicateMasker implements Masker<CharSequence, String> {
    private final Pattern pattern;
    private final Masker<? super CharSequence, ? extends String> masker;

    public static StringPatternPredicateMasker compile(
            @NonNull String regex,
            @NonNull Masker<? super CharSequence, ? extends String> masker
    ) {
        return new StringPatternPredicateMasker(Pattern.compile(regex), masker);
    }

    public StringPatternPredicateMasker(
            @NonNull Pattern pattern,
            @NonNull Masker<? super CharSequence, ? extends String> masker
    ) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) throws Exception {
        return value != null && pattern.matcher(value).matches()
                ? masker.applyMasking(value)
                : (value == null ? null : value.toString());
    }
}
