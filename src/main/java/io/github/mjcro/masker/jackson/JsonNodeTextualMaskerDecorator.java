package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class JsonNodeTextualMaskerDecorator implements Masker<JsonNode, JsonNode> {
    private final Masker<String, String> masker;

    public JsonNodeTextualMaskerDecorator(@NonNull Masker<String, String> masker) {
        this.masker = Objects.requireNonNull(masker, "masker");
    }

    @Override
    public @Nullable JsonNode applyMasking(@Nullable JsonNode value) throws Exception {
        if (value == null) {
            return null;
        }

        if (!value.isTextual()) {
            return value;
        }

        final String original = value.textValue();
        final String masked = masker.applyMasking(original);
        if (masked == original) {
            return value;
        }
        if (masked == null) {
            return NullNode.getInstance();
        }
        return new TextNode(masked);
    }
}
