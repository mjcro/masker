package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks HTTP {@code Authorization} style header values.
 * Preserves the scheme (everything up to the first space) and the first 3 characters
 * of the credential, then appends the mask token — e.g. {@code Bearer abc***}.
 * Inputs that do not look like a scheme-prefixed credential, blanks and {@code null}
 * are delegated to the fallback masker.
 */
public class StringAuthorizationHeaderMasker implements Masker<String, String> {
    public static final StringAuthorizationHeaderMasker DEFAULT = new StringAuthorizationHeaderMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    /**
     * Constructs new authorization header masker.
     *
     * @param masker Non-null fallback masker supplying the mask token and handling edge cases.
     */
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
