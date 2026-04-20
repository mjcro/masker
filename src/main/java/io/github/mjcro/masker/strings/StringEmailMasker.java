package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks the local part of an email address while preserving the {@code @} separator
 * and the domain. For inputs without an {@code @} or with a very short local part
 * it falls back to the underlying smart-length masker.
 */
public class StringEmailMasker implements Masker<String, String> {
    public static final StringEmailMasker DEFAULT = new StringEmailMasker(StringSmartLengthMasker.DEFAULT);

    private final StringSmartLengthMasker masker;

    /**
     * Constructs new email masker.
     *
     * @param masker Non-null smart-length masker used for the local part and as a fallback.
     */
    public StringEmailMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank() || value.length() < 4) {
            return masker.applyMasking(value);
        }

        int index = value.indexOf("@");
        return index < 2
                ? masker.applyMasking(value)
                : masker.applyMasking(value.substring(0, index)) + value.substring(index);
    }
}
