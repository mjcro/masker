package io.github.mjcro.masker.formdata;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.NameMatchingMaskerDecorator;
import io.github.mjcro.masker.rules.Rulebook;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Masker for {@code application/x-www-form-urlencoded} payloads.
 * Splits the input on {@code &}, URL-decodes each value using the configured charset,
 * tries name-equals/name-contains maskers against the decoded key, then inline maskers,
 * then the optional default masker, and finally re-encodes any value that changed.
 * Key insertion order and duplicate keys are preserved. When no value is rewritten the
 * very same input reference is returned, so callers can compare with {@code ==}.
 * <p>
 * Keys with no {@code =} (bare flags) and pairs whose value did not change are emitted
 * verbatim from the source string, so the original encoding form is kept for untouched
 * data; rewritten pairs are re-encoded canonically (e.g. {@code %20} becomes {@code +}).
 */
public class FormDataStringMasker implements Masker<String, String> {
    private final List<Masker<Map.Entry<String, String>, String>> fieldMaskers;
    private final List<Masker<String, String>> inlineMaskers;
    private final @Nullable Masker<String, String> defaultMasker;
    private final Charset charset;

    /**
     * Assembles a form-data masker from the given rulebook.
     * Name-equals and name-contains maskers become field-level maskers, inline maskers
     * and the default masker are forwarded as-is; the rulebook's charset drives the
     * URL decode/encode round-trip.
     *
     * @param rulebook Non-null rulebook describing which fields to mask and how.
     * @return Ready-to-use form-data masker.
     */
    public static FormDataStringMasker usingRulebook(@NonNull Rulebook rulebook) {
        ArrayList<Masker<Map.Entry<String, String>, String>> fieldMaskers = new ArrayList<>();
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameEqualsMaskers()) {
            fieldMaskers.add(NameMatchingMaskerDecorator.equalsCaseInsensitive(e.getValue(), e.getKey()));
        }
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameContainsMaskers()) {
            fieldMaskers.add(NameMatchingMaskerDecorator.containsCaseInsensitive(e.getValue(), e.getKey()));
        }
        return new FormDataStringMasker(
                rulebook.getCharset(),
                fieldMaskers,
                rulebook.getInlineMaskers(),
                rulebook.getDefaultMasker()
        );
    }

    /**
     * Constructs new form-data masker.
     *
     * @param charset       Non-null charset used for URL decode/encode.
     * @param fieldMaskers  Non-null list of maskers accepting (decoded name, decoded value) entries.
     * @param inlineMaskers Non-null list of maskers applied to every decoded value when no field rule matched.
     * @param defaultMasker Optional fallback applied when no field nor inline masker changed the value.
     */
    public FormDataStringMasker(
            @NonNull Charset charset,
            @NonNull List<Masker<Map.Entry<String, String>, String>> fieldMaskers,
            @NonNull List<Masker<String, String>> inlineMaskers,
            @Nullable Masker<String, String> defaultMasker
    ) {
        this.fieldMaskers = Objects.requireNonNull(fieldMaskers, "fieldMaskers");
        this.inlineMaskers = Objects.requireNonNull(inlineMaskers, "inlineMaskers");
        this.defaultMasker = defaultMasker;
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder sb = new StringBuilder(value.length());
        boolean anyChanged = false;
        int i = 0;
        final int n = value.length();
        boolean first = true;
        while (i <= n) {
            int amp = value.indexOf('&', i);
            int end = amp == -1 ? n : amp;
            int eq = value.indexOf('=', i);

            String rawName;
            String rawValue;
            boolean hasEquals;
            if (eq != -1 && eq < end) {
                rawName = value.substring(i, eq);
                rawValue = value.substring(eq + 1, end);
                hasEquals = true;
            } else {
                rawName = value.substring(i, end);
                rawValue = null;
                hasEquals = false;
            }

            if (!first) {
                sb.append('&');
            }
            first = false;

            if (hasEquals) {
                String decodedName = URLDecoder.decode(rawName, charset);
                String decodedValue = URLDecoder.decode(rawValue, charset);
                String masked = maskValue(decodedName, decodedValue);
                if (masked != decodedValue) {
                    anyChanged = true;
                    sb.append(URLEncoder.encode(decodedName, charset));
                    sb.append('=');
                    if (masked != null) {
                        sb.append(URLEncoder.encode(masked, charset));
                    }
                } else {
                    sb.append(rawName);
                    sb.append('=');
                    sb.append(rawValue);
                }
            } else {
                sb.append(rawName);
            }

            if (amp == -1) {
                break;
            }
            i = amp + 1;
        }

        return anyChanged ? sb.toString() : value;
    }

    private @Nullable String maskValue(@NonNull String name, @NonNull String decodedValue) throws Exception {
        Map.Entry<String, String> entry = Map.entry(name, decodedValue);
        for (Masker<Map.Entry<String, String>, String> m : fieldMaskers) {
            String transformed = m.applyMasking(entry);
            if (transformed != decodedValue) {
                return transformed;
            }
        }
        for (Masker<String, String> m : inlineMaskers) {
            String transformed = m.applyMasking(decodedValue);
            if (transformed != decodedValue) {
                return transformed;
            }
        }
        if (defaultMasker != null) {
            String transformed = defaultMasker.applyMasking(decodedValue);
            if (transformed != decodedValue) {
                return transformed;
            }
        }
        return decodedValue;
    }
}
