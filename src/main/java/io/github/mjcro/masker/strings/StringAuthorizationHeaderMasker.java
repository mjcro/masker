package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masker for HTTP authorization headers.
 */
public class StringAuthorizationHeaderMasker implements Masker<String, String> {
    public static final StringAuthorizationHeaderMasker DEFAULT = new StringAuthorizationHeaderMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    public StringAuthorizationHeaderMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker);
    }

    @Override
    public String applyMasking(@Nullable String value) throws Exception {
        if (value == null || value.isBlank()) {
            return masker.applyMasking(value);
        }

        int index = value.indexOf(' ');

        if (value.length() < 10 || index < 2 || index > value.length() - 5) {
            return masker.applyMasking(value);
        }

        return value.substring(0, index + 4) + masker.getMask();
    }
}
