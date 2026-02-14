package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks credit card number (PAN) leaving only 4 last digits.
 */
public class StringCardPanMasker implements Masker<CharSequence, String> {
    public static final StringCardPanMasker DEFAULT = new StringCardPanMasker(StringSmartLengthMasker.DEFAULT);

    private final StringSmartLengthMasker masker;

    public StringCardPanMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) {
        if (value == null) {
            return masker.applyMasking(null);
        }

        String s = value.toString().replaceAll("[^0-9]", "").strip();
        if (s.isBlank() || s.length() < 12 || s.length() > 19) {
            return masker.applyMasking(s);
        }

        // Returning LAST4
        return masker.getMask() + s.substring(s.length() - 4);
    }
}
