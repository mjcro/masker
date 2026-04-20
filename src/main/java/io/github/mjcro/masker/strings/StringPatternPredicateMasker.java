package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Conditional decorator that delegates to the wrapped masker only when the whole input
 * matches the given regular expression ({@link java.util.regex.Matcher#matches} semantics).
 * Non-matching and {@code null} inputs pass through unchanged.
 */
public class StringPatternPredicateMasker implements Masker<String, String> {
    private final Pattern pattern;
    private final Masker<String, String> masker;

    /**
     * Convenience factory compiling the regex on the fly.
     *
     * @param regex  Non-null regex pattern string.
     * @param masker Non-null masker to invoke on match.
     * @return New decorator instance.
     */
    public static StringPatternPredicateMasker compile(
            @NonNull String regex,
            @NonNull Masker<String, String> masker
    ) {
        return new StringPatternPredicateMasker(Pattern.compile(regex), masker);
    }

    /**
     * Constructs new predicate masker.
     *
     * @param pattern Non-null compiled pattern.
     * @param masker  Non-null masker to invoke on full match.
     */
    public StringPatternPredicateMasker(
            @NonNull Pattern pattern,
            @NonNull Masker<String, String> masker
    ) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) throws Exception {
        return value != null && pattern.matcher(value).matches()
                ? masker.applyMasking(value)
                : value;
    }
}
