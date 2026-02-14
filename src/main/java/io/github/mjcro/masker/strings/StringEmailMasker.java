package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class StringEmailMasker implements Masker<String, String> {
    public static final StringEmailMasker DEFAULT = new StringEmailMasker(StringSmartLengthMasker.DEFAULT);

    private final StringSmartLengthMasker masker;

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
