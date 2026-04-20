package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Masks an IBAN keeping the first 4 characters (country code + check digits) and,
 * for values long enough, the last 4 characters.
 * Spaces and hyphens are stripped before processing. Inputs outside the 11–40 char
 * range, blanks and {@code null} are delegated to the fallback masker.
 */
public class StringIbanMasker implements Masker<String, String> {
    public static final StringIbanMasker DEFAULT = new StringIbanMasker(StringSmartLengthMasker.DEFAULT);
    private static final Pattern inlinePattern = Pattern.compile("\\b[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}\\b");
    private static final Pattern SEPARATORS = Pattern.compile("[ -]");

    private final StringSmartLengthMasker masker;

    /**
     * Constructs new IBAN masker.
     *
     * @param masker Non-null fallback masker for non-IBAN inputs and for the mask token.
     */
    public StringIbanMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    /**
     * Wraps this masker as an inline pattern masker that finds IBANs embedded in larger text.
     *
     * @return Non-null inline masker matching {@code [A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}}.
     */
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
