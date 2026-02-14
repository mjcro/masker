package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.Nullable;

/**
 * Truncates long strings with length exceeding threshold, leaving prefix,
 * suffix and adding information about skipped characters
 */
public class StringLongTruncationMasker implements Masker<String, String> {
    public static final StringLongTruncationMasker DEFAULT = new StringLongTruncationMasker(256);
    private final int threshold;

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
