package io.github.mjcro.masker.auto;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.formdata.FormDataStringMasker;
import io.github.mjcro.masker.jackson.JsonNodeDocumentMasker;
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.xml.XmlStringStaxMasker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Auto-detecting body masker that inspects the input string and dispatches to the
 * appropriate document-level masker.
 *
 * <p>Detection is based on the first non-whitespace character:
 * <ul>
 *   <li>{@code '{'} or {@code '['} &rarr; routed to a {@link JsonNodeDocumentMasker}</li>
 *   <li>{@code '<'} &rarr; routed to an {@link XmlStringStaxMasker}</li>
 *   <li>anything else &rarr; routed to a {@link FormDataStringMasker}</li>
 * </ul>
 *
 * <p>{@code null}, empty and blank inputs are returned unchanged. Detection is
 * intentionally cheap and does not validate the payload &mdash; malformed JSON or
 * XML will surface as an exception from the underlying masker.
 *
 * <p>Because the JSON and XML masking paths re-serialize the payload via Jackson
 * and StAX respectively, the original whitespace, encoding form and element
 * order may not be preserved even when no value is rewritten. Only the form-data
 * branch keeps the source string verbatim when nothing changes.
 */
public class AutoBodyMasker implements Masker<String, String> {
    private final FormDataStringMasker formDataStringMasker;
    private final JsonNodeDocumentMasker jsonNodeDocumentMasker;
    private final XmlStringStaxMasker xmlStringStaxMasker;

    /**
     * Assembles an auto-detecting masker by building each of the three underlying
     * document maskers from the given rulebook.
     *
     * @param rulebook Non-null rulebook supplying charset, name maskers and inline maskers.
     * @return Ready-to-use auto-detecting body masker.
     */
    public static AutoBodyMasker usingRulebook(@NonNull Rulebook rulebook) {
        Objects.requireNonNull(rulebook, "rulebook");
        return new AutoBodyMasker(
                FormDataStringMasker.usingRulebook(rulebook),
                JsonNodeDocumentMasker.usingRulebook(rulebook),
                XmlStringStaxMasker.usingRulebook(rulebook)
        );
    }

    /**
     * Constructs new auto-detecting masker from pre-built document maskers.
     *
     * @param formDataStringMasker   Non-null masker used for {@code application/x-www-form-urlencoded} payloads.
     * @param jsonNodeDocumentMasker Non-null masker used for JSON payloads.
     * @param xmlStringStaxMasker    Non-null masker used for XML payloads.
     */
    public AutoBodyMasker(
            @NonNull FormDataStringMasker formDataStringMasker,
            @NonNull JsonNodeDocumentMasker jsonNodeDocumentMasker,
            @NonNull XmlStringStaxMasker xmlStringStaxMasker
    ) {
        this.formDataStringMasker = Objects.requireNonNull(formDataStringMasker, "formDataStringMasker");
        this.jsonNodeDocumentMasker = Objects.requireNonNull(jsonNodeDocumentMasker, "jsonNodeDocumentMasker");
        this.xmlStringStaxMasker = Objects.requireNonNull(xmlStringStaxMasker, "xmlStringStaxMasker");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return value;
        }

        final int n = value.length();
        int i = 0;
        while (i < n && Character.isWhitespace(value.charAt(i))) {
            i++;
        }
        if (i == n) {
            return value;
        }

        switch (value.charAt(i)) {
            case '{':
            case '[':
                return jsonNodeDocumentMasker.maskJsonString(value);
            case '<':
                return xmlStringStaxMasker.applyMasking(value);
            default:
                return formDataStringMasker.applyMasking(value);
        }
    }
}
