package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleRulebook implements Rulebook {
    private final @NonNull Charset charset;
    private final @Nullable Masker<String, String> defaultMasker;
    private final @NonNull List<Masker<String, String>> inlineMaskers;
    private final @NonNull List<Map.Entry<String[], Masker<String, String>>> nameEqualsMaskers;
    private final @NonNull List<Map.Entry<String[], Masker<String, String>>> nameContainsMaskers;

    public SimpleRulebook(
            @Nullable Charset charset,
            @Nullable Masker<String, String> defaultMasker,
            @Nullable List<Masker<String, String>> inlineMaskers,
            @Nullable List<Map.Entry<String[], Masker<String, String>>> nameEqualsMaskers,
            @Nullable List<Map.Entry<String[], Masker<String, String>>> nameContainsMaskers
    ) {
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.defaultMasker = defaultMasker;
        this.inlineMaskers = inlineMaskers == null ? Collections.emptyList() : inlineMaskers;
        this.nameEqualsMaskers = nameEqualsMaskers == null ? Collections.emptyList() : nameEqualsMaskers;
        this.nameContainsMaskers = nameContainsMaskers == null ? Collections.emptyList() : nameContainsMaskers;
    }

    @Override
    public @NonNull Charset getCharset() {
        return charset;
    }

    @Override
    public @Nullable Masker<String, String> getDefaultMasker() {
        return defaultMasker;
    }

    @Override
    public @NonNull List<Masker<String, String>> getInlineMaskers() {
        return inlineMaskers;
    }

    @Override
    public @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameEqualsMaskers() {
        return nameEqualsMaskers;
    }

    @Override
    public @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameContainsMaskers() {
        return nameContainsMaskers;
    }
}
