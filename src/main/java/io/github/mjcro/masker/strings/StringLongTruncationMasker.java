package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.Nullable;

/**
 * Shortens strings longer than a configurable threshold by keeping a prefix and a short
 * suffix with a {@code [...N...]} marker describing the number of skipped characters.
 * Inputs at or below the threshold and {@code null} are returned unchanged.
 * Intended as a log-size guard rather than a privacy masker.
 */
public class StringLongTruncationMasker implements Masker<String, String> {
    public static final StringLongTruncationMasker DEFAULT = new StringLongTruncationMasker(256);
    private final int threshold;

    /**
     * Constructs new truncation masker.
     *
     * @param threshold Maximum length of the returned string. Must be at least 32.
     * @throws IllegalArgumentException When {@code threshold &lt; 32}.
     */
    public StringLongTruncationMasker(int threshold) {
        if (threshold < 32) {
            throw new IllegalArgumentException("Threshold should be at least 32 chars");
        }
        this.threshold = threshold;
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null) {
            return null;
        } else if (value.length() <= threshold) {
            return value;
        }

        return value.substring(0, threshold - 12)
                + "[..."
                + (value.length() - threshold + 14)
                + "...]"
                + value.substring(value.length() - 2);
    }
}
