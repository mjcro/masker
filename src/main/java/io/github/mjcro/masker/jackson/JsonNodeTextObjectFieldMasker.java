package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class JsonNodeTextObjectFieldMasker implements Masker<Map.Entry<String, JsonNode>, JsonNode> {
    private final Predicate<? super String> namePredicate;
    private final Masker<String, String> masker;

    public JsonNodeTextObjectFieldMasker(
            @NonNull Predicate<? super String> namePredicate,
            @NonNull Masker<String, String> masker
    ) {
        this.namePredicate = Objects.requireNonNull(namePredicate, "namePredicate");
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable JsonNode applyMasking(Map.@Nullable Entry<@NonNull String, @NonNull JsonNode> value)
            throws Exception {
        if (value == null) {
            return null;
        }

        JsonNode node = value.getValue();
        if (node.isTextual() && namePredicate.test(value.getKey())) {
            return new TextNode(masker.applyMasking(node.textValue()));
        }
        return node;
    }
}
