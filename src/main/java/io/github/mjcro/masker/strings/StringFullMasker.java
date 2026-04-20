package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Replaces the entire input string with a fixed mask token, except for
 * {@code null} and blank inputs which are returned unchanged.
 * The default mask token is {@value #MASK}.
 */
public class StringFullMasker implements Masker<String, String> {
    public static final String MASK = "***";
    public static final StringFullMasker DEFAULT = new StringFullMasker(MASK);
    private final String mask;

    /**
     * Constructs new full masker.
     *
     * @param mask Non-null mask token returned for every non-blank input.
     */
    public StringFullMasker(@NonNull String mask) {
        this.mask = Objects.requireNonNull(mask, "mask");
    }

    /**
     * @return Non-null mask token used by this masker.
     */
    public String getMask() {
        return mask;
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null) {
            return null;
        } else if (value.isBlank()) {
            return value;
        }

        return getMask();
    }
}
