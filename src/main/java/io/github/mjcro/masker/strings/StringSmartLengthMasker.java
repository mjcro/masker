package io.github.mjcro.masker.strings;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Masks a string preserving a few leading and/or trailing characters depending on length.
 * Shape summary:
 * <ul>
 *   <li>length &lt; 4 or blank: delegated to full masker.</li>
 *   <li>length &lt; 10: first char + mask token.</li>
 *   <li>length &lt; 20: first char + mask token + last char.</li>
 *   <li>length &lt; 40: first 2 chars + mask token + last char.</li>
 *   <li>length ≥ 40: first 4 chars + mask token + last char.</li>
 * </ul>
 * {@code null} input stays {@code null}.
 */
public class StringSmartLengthMasker implements Masker<String, String> {
    public static final StringSmartLengthMasker DEFAULT = new StringSmartLengthMasker(StringFullMasker.DEFAULT);
    private final StringFullMasker fullMasker;

    /**
     * Constructs new smart-length masker.
     *
     * @param fullMasker Non-null full masker supplying the mask token and the short-input fallback.
     */
    public StringSmartLengthMasker(@NonNull StringFullMasker fullMasker) {
        this.fullMasker = Objects.requireNonNull(fullMasker);
    }

    /**
     * @return Non-null mask token used by this masker.
     */
    public String getMask() {
        return fullMasker.getMask();
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) {
        if (value == null || value.length() < 4) {
            return fullMasker.applyMasking(value);
        }
        if (value.isBlank()) {
            return fullMasker.applyMasking(value);
        } else if (value.length() < 10) {
            return value.charAt(0) + fullMasker.getMask();
        } else if (value.length() < 20) {
            return value.charAt(0) + fullMasker.getMask() + value.charAt(value.length() - 1);
        } else if (value.length() < 40) {
            return value.substring(0, 2) + fullMasker.getMask() + value.charAt(value.length() - 1);
        } else {
            return value.substring(0, 4) + fullMasker.getMask() + value.charAt(value.length() - 1);
        }
    }
}
