package io.github.mjcro.masker.headers;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.util.EqualsCaseInsensitivePredicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Masker for HTTP header maps of shape {@code Map<String, List<String>>}.
 * Looks up a per-name masker using the lowercased header name; when no rule matches
 * and a default masker is configured, the default is used.
 * Preserves insertion order of the input map and returns a new {@link LinkedHashMap}.
 * {@code null} or empty inputs are passed through unchanged.
 */
public class HeadersPerNameMasker implements Masker<Map<String, List<String>>, Map<String, List<String>>> {
    private final Map<String, Masker<? super String, ? extends String>> rules;
    private final Masker<? super String, ? extends String> defaultMasker;

    /**
     * Assembles a header masker from the given rulebook.
     * Only name-equals maskers and the default masker are consumed; other rulebook
     * categories are ignored.
     *
     * @param rulebook Non-null rulebook.
     * @return Ready-to-use header masker.
     */
    public static HeadersPerNameMasker usingRulebook(@NonNull Rulebook rulebook) {
        HashMap<String, Masker<? super String, ? extends String>> nameRules = new HashMap<>();
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameEqualsMaskers()) {
            for (String name : e.getKey()) {
                nameRules.put(name, e.getValue());
            }
        }
        return new HeadersPerNameMasker(nameRules, rulebook.getDefaultMasker());
    }

    /**
     * Constructs new header masker.
     *
     * @param rules         Non-null map from header name to masker. Names are lowercased internally.
     * @param defaultMasker Optional fallback masker applied when the name has no dedicated rule.
     */
    public HeadersPerNameMasker(
            @NonNull Map<String, Masker<? super String, ? extends String>> rules,
            @Nullable Masker<? super String, ? extends String> defaultMasker
    ) {
        this.rules = rules.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        $ -> EqualsCaseInsensitivePredicate.toLowerCase($.getKey()),
                        Map.Entry::getValue
                ));
        this.defaultMasker = defaultMasker;
    }

    @Override
    public @Nullable Map<String, List<String>> applyMasking(@Nullable Map<String, List<String>> in) throws Exception {
        if (in == null || in.isEmpty()) {
            return in;
        }

        LinkedHashMap<String, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : in.entrySet()) {
            String name = EqualsCaseInsensitivePredicate.toLowerCase(e.getKey());
            List<String> values = e.getValue();
            Masker<? super String, ? extends String> masker = rules.get(name);
            if (masker == null) {
                masker = defaultMasker;
            }
            out.put(
                    e.getKey(),
                    masker != null && values != null && !values.isEmpty()
                            ? maskList(masker, values)
                            : values
            );
        }
        return out;
    }

    private List<String> maskList(Masker<? super String, ? extends String> masker, List<String> in) throws Exception {
        ArrayList<String> out = new ArrayList<>();
        for (String s : in) {
            out.add(masker.applyMasking(s));
        }
        return out;
    }
}
