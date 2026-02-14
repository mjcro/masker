package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masker for HTTP authorization headers.
 */
public class StringAuthorizationHeaderMasker implements Masker<CharSequence, String> {
    public static final StringAuthorizationHeaderMasker DEFAULT = new StringAuthorizationHeaderMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    public StringAuthorizationHeaderMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker);
    }

    @Override
    public String applyMasking(@Nullable CharSequence data) throws Exception {
        if (data == null) {
            return null;
        }

        String s = data.toString();
        int index = s.indexOf(' ');

        if (s.length() < 10 || index < 2 || index > s.length() - 5) {
            return masker.applyMasking(data);
        }

        return s.substring(0, index + 4) + masker.getMask();
    }
}
