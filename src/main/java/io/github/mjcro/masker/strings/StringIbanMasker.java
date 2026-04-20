package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Masks IBAN leaving only 4 first and last digits.
 */
public class StringIbanMasker implements Masker<String, String> {
    public static final StringIbanMasker DEFAULT = new StringIbanMasker(StringSmartLengthMasker.DEFAULT);
    private static final Pattern inlinePattern = Pattern.compile("\\b[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}\\b");
    private static final Pattern SEPARATORS = Pattern.compile("[ -]");

    private final StringSmartLengthMasker masker;

    public StringIbanMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    public StringInlinePatternMasker asInlineMasker() {
        return new StringInlinePatternMasker(inlinePattern, this);
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return masker.applyMasking(value);
        }

        final String s = SEPARATORS.matcher(value).replaceAll("").strip();
        if (s.isBlank() || s.length() < 11 || s.length() > 40) {
            return masker.applyMasking(s);
        }

        return s.length() < 18
                ? s.substring(0, 4) + masker.getMask()
                : s.substring(0, 4) + masker.getMask() + s.substring(s.length() - 4);
    }
}
