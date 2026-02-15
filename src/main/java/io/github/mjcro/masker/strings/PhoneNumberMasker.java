package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks phone numbers leaving last two digits only.
 */
public class PhoneNumberMasker implements Masker<String, String> {
    public static final PhoneNumberMasker DEFAULT = new PhoneNumberMasker(StringFullMasker.DEFAULT);

    private final StringFullMasker fallback;

    public PhoneNumberMasker(@NonNull StringFullMasker fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.isBlank() || value.length() < 6) {
            return fallback.applyMasking(value);
        }

        value = value.replaceAll("[^0-9]", "").strip();
        return value.length() < 7
                ? fallback.applyMasking(value)
                : fallback.getMask() + value.substring(value.length() - 2);
    }
}
