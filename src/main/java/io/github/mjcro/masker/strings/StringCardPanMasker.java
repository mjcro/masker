package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Masks a credit card number (PAN) keeping only the last four digits and prefixing them
 * with the fallback masker's mask token (e.g. {@code ***1234}).
 * All non-digit characters are stripped before length validation (12–19 digits).
 * Values shorter/longer than that range, blanks and {@code null} are delegated to the fallback masker.
 */
public class StringCardPanMasker implements Masker<String, String> {
    public static final StringCardPanMasker DEFAULT = new StringCardPanMasker(StringSmartLengthMasker.DEFAULT);
    private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]");

    private final StringSmartLengthMasker masker;

    /**
     * Constructs new PAN masker.
     *
     * @param masker Non-null fallback masker used for non-PAN inputs and for the mask token.
     */
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
