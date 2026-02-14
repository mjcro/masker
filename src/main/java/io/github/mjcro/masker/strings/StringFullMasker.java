package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Fully replaces given char sequence with mask, excluding two cases:
 * - Null values are left as-is
 * - Empty strings are left as-is
 */
public class StringFullMasker implements Masker<CharSequence, String> {
    public static final String MASK = "***";
    public static final StringFullMasker DEFAULT = new StringFullMasker(MASK);
    private final String mask;

    public StringFullMasker(@NonNull String mask) {
        this.mask = Objects.requireNonNull(mask, "mask");
    }

    public String getMask() {
        return mask;
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) {
        if (value == null) {
            return null;
        } else if (value.length() == 0) {
            return "";
        }
        String s = value.toString();

        return s.isBlank()
                ? s
                : getMask();
    }
}
