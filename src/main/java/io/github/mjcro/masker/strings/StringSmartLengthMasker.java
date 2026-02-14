package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks string, leaving short unchanged prefix and suffix for long strings.
 */
public class StringSmartLengthMasker implements Masker<String, String> {
    public static final StringSmartLengthMasker DEFAULT = new StringSmartLengthMasker(StringFullMasker.DEFAULT);
    private final StringFullMasker fullMasker;

    public StringSmartLengthMasker(@NonNull StringFullMasker fullMasker) {
        this.fullMasker = Objects.requireNonNull(fullMasker);
    }

    public String getMask() {
        return fullMasker.getMask();
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.length() < 4) {
            return fullMasker.applyMasking(value);
        }
        if (value.isBlank()) {
            return fullMasker.applyMasking(value);
        } else if (value.length() < 10) {
            return value.charAt(0) + fullMasker.getMask();
        } else if (value.length() < 20) {
            return value.charAt(0) + fullMasker.getMask() + value.charAt(value.length() - 1);
        } else if (value.length() < 40) {
            return value.substring(0, 2) + fullMasker.getMask() + value.charAt(value.length() - 1);
        } else {
            return value.substring(0, 4) + fullMasker.getMask() + value.charAt(value.length() - 1);
        }
    }
}
