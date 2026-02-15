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
 * Contains set of rules to be used by maskers.
 * Not all but some complex maskers supports rulebooks.
 * <p>
 * By design, maskers CAN support ANY of rules from rulebook.
 * There is no strict requirement to support all rules.
 */
public interface Rulebook {
    /**
     * Constructs {@link Map.Entry} tuple.
     *
     * @param masker Masker instance.
     * @param values String values (like names).
     * @param <T>    Masker type.
     * @return Map entry tuple.
     */
    static <T> Map.Entry<String[], Masker<T, T>> tuple(
            @NonNull Masker<T, T> masker,
            @NonNull String @NonNull ... values
    ) {
        return Map.entry(values, masker);
    }

    /**
     * @return Charset masker should work with (if applicable).
     */
    default Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    /**
     * @return Default masker to use.
     */
    default @Nullable Masker<String, String> getDefaultMasker() {
        return null;
    }

    /**
     * @return Inline maskers to use.
     */
    default @NonNull List<Masker<String, String>> getInlineMaskers() {
        return Collections.emptyList();
    }

    /**
     * @return Maskers matching by (object field/header) name using equality check.
     */
    default @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameEqualsMaskers() {
        return Collections.emptyList();
    }

    /**
     * @return Maskers matching by (object field/header) name using contains check.
     */
    default @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameContainsMaskers() {
        return Collections.emptyList();
    }
}
