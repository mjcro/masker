package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class StringEmailMasker implements Masker<CharSequence, String> {
    public static final StringEmailMasker DEFAULT = new StringEmailMasker(StringSmartLengthMasker.DEFAULT);

    private final StringSmartLengthMasker masker;

    public StringEmailMasker(@NonNull StringSmartLengthMasker masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable CharSequence value) {
        if (value == null || value.length() < 4) {
            return masker.applyMasking(value);
        }

        String s = value.toString();
        int index = s.indexOf("@");
        return masker.applyMasking(s.substring(0, index)) + s.substring(index);
    }
}
