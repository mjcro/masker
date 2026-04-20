package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Masks a phone number keeping only the last two digits, prefixed with the fallback mask token.
 * Non-digit characters are stripped before processing. Inputs shorter than 7 digits,
 * blanks and {@code null} are delegated to the fallback masker.
 */
public class PhoneNumberMasker implements Masker<String, String> {
    public static final PhoneNumberMasker DEFAULT = new PhoneNumberMasker(StringFullMasker.DEFAULT);
    private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]");

    private final StringFullMasker fallback;

    /**
     * Constructs new phone masker.
     *
     * @param fallback Non-null fallback masker used for short inputs and for the mask token.
     */
    public PhoneNumberMasker(@NonNull StringFullMasker fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank() || value.length() < 6) {
            return fallback.applyMasking(value);
        }

        value = NON_DIGITS.matcher(value).replaceAll("").strip();
        return value.length() < 7
                ? fallback.applyMasking(value)
                : fallback.getMask() + value.substring(value.length() - 2);
    }
}
