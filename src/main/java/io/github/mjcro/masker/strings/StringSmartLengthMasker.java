package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks string, leaving short unchanged prefix and suffix for long strings.
 */
public class StringSmartLengthMasker implements Masker<CharSequence, String> {
    public static final StringSmartLengthMasker DEFAULT = new StringSmartLengthMasker(StringFullMasker.DEFAULT);
    private final StringFullMasker fullMasker;

    public StringSmartLengthMasker(@NonNull StringFullMasker fullMasker) {
        this.fullMasker = Objects.requireNonNull(fullMasker);
    }

    public String getMask() {
        return fullMasker.getMask();
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) {
        if (value == null || value.length() < 4) {
            return fullMasker.applyMasking(value);
        }
        String s = value.toString();
        if (s.isBlank()) {
            return fullMasker.applyMasking(s);
        } else if (s.length() < 10) {
            return s.charAt(0) + fullMasker.getMask();
        } else if (s.length() < 20) {
            return s.charAt(0) + fullMasker.getMask() + s.charAt(s.length() - 1);
        } else if (s.length() < 40) {
            return s.substring(0, 2) + fullMasker.getMask() + s.charAt(s.length() - 1);
        } else {
            return s.substring(0, 4) + fullMasker.getMask() + s.charAt(s.length() - 1);
        }
    }
}
