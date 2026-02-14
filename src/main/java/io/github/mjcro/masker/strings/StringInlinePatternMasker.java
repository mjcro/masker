package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces inline values matched by Regex within string.
 */
public class StringInlinePatternMasker implements Masker<String, String> {
    private final Pattern pattern;
    private final Masker<String, String> masker;

    public static StringInlinePatternMasker compile(
            @NonNull String regex,
            @NonNull Masker<String, String> masker
    ) {
        return new StringInlinePatternMasker(Pattern.compile(regex), masker);
    }

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

        Matcher matcher = pattern.matcher(value);
        StringBuilder sb = new StringBuilder(value.length());
        while (matcher.find()) {
            String match = matcher.group(0);
            String replacement = masker.applyMasking(match);
            matcher.appendReplacement(sb, replacement == null ? match : replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
