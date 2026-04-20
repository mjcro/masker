package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Masks credit card number (PAN) leaving only 4 last digits.
 */
public class StringCardPanMasker implements Masker<String, String> {
    public static final StringCardPanMasker DEFAULT = new StringCardPanMasker(StringSmartLengthMasker.DEFAULT);
    private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]");

    private final StringSmartLengthMasker masker;

    public StringCardPanMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return masker.applyMasking(value);
        }

        value = NON_DIGITS.matcher(value).replaceAll("").strip();
        if (value.length() < 12 || value.length() > 19) {
            return masker.applyMasking(value);
        }

        // Returning LAST4
        return masker.getMask() + value.substring(value.length() - 4);
    }
}
