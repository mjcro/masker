package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInlinePatterMasker implements Masker<CharSequence, String> {
    private final Pattern pattern;
    private final Masker<? super CharSequence, ? extends String> masker;

    public static StringInlinePatterMasker compile(
            @NonNull String regex,
            @NonNull Masker<? super CharSequence, ? extends String> masker
    ) {
        return new StringInlinePatterMasker(Pattern.compile(regex), masker);
    }

    public StringInlinePatterMasker(
            @NonNull Pattern pattern,
            @NonNull Masker<? super CharSequence, ? extends String> masker
    ) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) throws Exception {
        if (value == null) {
            return null;
        }

        String s = value.toString();
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            String substring = matcher.group(1);
            String replacement = masker.applyMasking(substring);
            s = s.replaceAll(Pattern.quote(substring), replacement == null ? "" : replacement);
        }

        return s;
    }
}
