package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds every regex occurrence inside the input string and passes the matched substring
 * through the wrapped masker. The returned string keeps the non-matching parts untouched.
 * When no match is found, or no replacement differs from the original match, the original
 * reference is returned so downstream reference-equality checks can short-circuit.
 * {@code null} and blank inputs pass through unchanged.
 */
public class StringInlinePatternMasker implements Masker<String, String> {
    private final Pattern pattern;
    private final Masker<String, String> masker;

    /**
     * Convenience factory compiling the regex on the fly.
     *
     * @param regex  Non-null regex pattern string.
     * @param masker Non-null masker invoked on every match.
     * @return New inline masker instance.
     */
    public static StringInlinePatternMasker compile(
            @NonNull String regex,
            @NonNull Masker<String, String> masker
    ) {
        return new StringInlinePatternMasker(Pattern.compile(regex), masker);
    }

    /**
     * Constructs new inline masker.
     *
     * @param pattern Non-null compiled pattern.
     * @param masker  Non-null masker invoked on every match.
     */
    public StringInlinePatternMasker(
            @NonNull Pattern pattern,
            @NonNull Masker<String, String> masker
    ) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) throws Exception {
        if (value == null || value.isBlank()) {
            return value;
        }

        final Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return value;
        }

        boolean changed = false;
        final StringBuilder sb = new StringBuilder(value.length());
        do {
            final String match = matcher.group(0);
            final String raw = masker.applyMasking(match);
            final String replacement = raw == null ? match : raw;
            if (!replacement.equals(match)) {
                changed = true;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        } while (matcher.find());
        matcher.appendTail(sb);

        return changed ? sb.toString() : value;
    }
}
