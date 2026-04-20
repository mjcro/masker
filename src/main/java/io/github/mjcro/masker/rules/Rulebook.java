package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configuration bundle consumed by document-level maskers to decide which
 * leaf masker to apply to a given field, header or textual fragment.
 *
 * <p>A rulebook exposes four independent categories of rules together with a
 * charset hint:
 * <ul>
 *   <li><strong>Name-equals maskers</strong> — applied when a field or header
 *       name matches one of the configured names exactly (case-insensitive).</li>
 *   <li><strong>Name-contains maskers</strong> — applied when a field or header
 *       name contains one of the configured substrings (case-insensitive).</li>
 *   <li><strong>Inline maskers</strong> — applied unconditionally to every
 *       textual leaf, typically used for pattern-based detection such as
 *       card numbers embedded in free-form text.</li>
 *   <li><strong>Default masker</strong> — fallback applied when no other rule
 *       matches; category support is consumer-specific.</li>
 * </ul>
 *
 * <p>Consumers are <em>not</em> required to honour every category; each
 * document-level masker documents which categories it observes. Implementations
 * are therefore free to expose only the subset of rules relevant to their use
 * case, and missing categories default to safe empty values.
 *
 * <p>Implementations are expected to be immutable and safe for concurrent use.
 *
 * @since 0.0.1
 */
public interface Rulebook {
    /**
     * Builds a {@code (names, masker)} tuple suitable for
     * {@link #getNameEqualsMaskers()} and {@link #getNameContainsMaskers()}.
     *
     * @param masker masker to associate with the given names; must not be {@code null}.
     * @param values names or name fragments to match; must not be {@code null}.
     * @param <T>    masker value type.
     * @return immutable map entry pairing {@code values} with {@code masker}.
     * @throws NullPointerException if {@code masker} or {@code values} is {@code null}.
     */
    static <T> Map.Entry<String[], Masker<T, T>> tuple(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... values
    ) {
        return Map.entry(values, masker);
    }

    /**
     * Returns the charset used by maskers operating on encoded byte streams
     * (for example, the XML masker). Defaults to {@link StandardCharsets#UTF_8}.
     *
     * @return charset to use; never {@code null}.
     */
    default Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    /**
     * Returns the fallback masker applied when no other rule matches.
     *
     * <p>Support for this category is consumer-specific; see the documentation
     * of the consuming document-level masker for details.
     *
     * @return default masker, or {@code null} if no fallback is configured.
     */
    default @Nullable Masker<String, String> getDefaultMasker() {
        return null;
    }

    /**
     * Returns maskers applied unconditionally to every textual leaf, regardless
     * of its field or header name.
     *
     * @return immutable list of inline maskers; never {@code null}, possibly empty.
     */
    default @NonNull List<Masker<String, String>> getInlineMaskers() {
        return Collections.emptyList();
    }

    /**
     * Returns maskers keyed by names that must match exactly
     * (case-insensitive) to trigger the associated masker.
     *
     * @return immutable list of {@code (names, masker)} tuples; never
     * {@code null}, possibly empty.
     */
    default @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameEqualsMaskers() {
        return Collections.emptyList();
    }

    /**
     * Returns maskers keyed by substrings; the associated masker is triggered
     * when the field or header name contains any of the substrings
     * (case-insensitive).
     *
     * @return immutable list of {@code (substrings, masker)} tuples; never
     * {@code null}, possibly empty.
     */
    default @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameContainsMaskers() {
        return Collections.emptyList();
    }
}
