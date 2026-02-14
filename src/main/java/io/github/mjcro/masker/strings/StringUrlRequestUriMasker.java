package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masker that hides request URI part of given URL.
 */
public class StringUrlRequestUriMasker implements Masker<String, String> {
    public static final StringUrlRequestUriMasker DEFAULT = new StringUrlRequestUriMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    public StringUrlRequestUriMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return masker.applyMasking(value);
        }

        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return masker.applyMasking(value);
        }

        int index = value.indexOf('/', 9);
        return index > 0 && index < value.length() - 1
                ? value.substring(0, index + 1) + masker.getMask()
                : value;
    }
}
