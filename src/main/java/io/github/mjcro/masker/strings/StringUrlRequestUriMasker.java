package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks the request URI portion of an HTTP/HTTPS URL while preserving scheme,
 * host and the root slash — e.g. {@code https://example.com/***}.
 * Inputs not starting with {@code http://} or {@code https://}, blanks and {@code null}
 * are delegated to the fallback masker.
 */
public class StringUrlRequestUriMasker implements Masker<String, String> {
    public static final StringUrlRequestUriMasker DEFAULT = new StringUrlRequestUriMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    /**
     * Constructs new URL masker.
     *
     * @param masker Non-null fallback masker supplying the mask token and edge-case handling.
     */
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
