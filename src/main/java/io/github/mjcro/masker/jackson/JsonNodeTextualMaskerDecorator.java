package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mjcro.masker.Masker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Adapter that applies a {@code String} masker to the textual content of a Jackson
 * {@link JsonNode}. Non-textual nodes are returned untouched.
 * The original node is returned by reference when the underlying masker leaves the text unchanged,
 * which is required by the reference-equality short-circuit used in {@link JsonNodeDocumentMasker}.
 * A {@code null} result from the masker is converted into {@link NullNode}.
 */
public class JsonNodeTextualMaskerDecorator implements Masker<JsonNode, JsonNode> {
    private final Masker<String, String> masker;

    /**
     * Constructs new decorator.
     *
     * @param masker Non-null string masker to apply to the node text.
     */
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
