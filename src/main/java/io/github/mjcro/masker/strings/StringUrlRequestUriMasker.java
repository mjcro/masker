package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masker that hides request URI part of given URL.
 */
public class StringUrlRequestUriMasker implements Masker<CharSequence, String> {
    public static final StringUrlRequestUriMasker DEFAULT = new StringUrlRequestUriMasker(
            StringSmartLengthMasker.DEFAULT
    );

    private final StringSmartLengthMasker masker;

    public StringUrlRequestUriMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) {
        if (value == null) {
            return null;
        }

        String s = value.toString();
        if (s.isBlank() || !(s.startsWith("http://") || s.startsWith("https://"))) {
            return masker.applyMasking(s);
        }

        int index = s.indexOf('/', 9);
        return index > 0 && index < s.length() - 1
                ? s.substring(0, index + 1) + masker.getMask()
                : s;
    }
}
