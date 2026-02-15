package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.NameMatchingMaskerDecorator;
import io.github.mjcro.masker.rules.Rulebook;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonNodeDocumentMasker implements Masker<JsonNode, JsonNode> {
    private final List<Masker<Map.Entry<String, JsonNode>, JsonNode>> fieldMaskers;
    private final List<Masker<String, String>> inlineMaskers;

    public static JsonNodeDocumentMasker usingRulebook(@NonNull Rulebook rulebook) {
        ArrayList<Masker<Map.Entry<String, JsonNode>, JsonNode>> fieldMaskers = new ArrayList<>();
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameEqualsMaskers()) {
            fieldMaskers.add(
                    NameMatchingMaskerDecorator.equalsCaseInsensitive(
                            new JsonNodeTextualMaskerDecorator(e.getValue()),
                            e.getKey()
                    )
            );
        }
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameContainsMaskers()) {
            fieldMaskers.add(
                    NameMatchingMaskerDecorator.containsCaseInsensitive(
                            new JsonNodeTextualMaskerDecorator(e.getValue()),
                            e.getKey()
                    )
            );
        }
        return new JsonNodeDocumentMasker(fieldMaskers, rulebook.getInlineMaskers());
    }

    public JsonNodeDocumentMasker(
            @NonNull List<Masker<Map.Entry<String, JsonNode>, JsonNode>> fieldMaskers,
            @NonNull List<Masker<String, String>> inlineMaskers
    ) {
        this.fieldMaskers = Objects.requireNonNull(fieldMaskers, "fieldMaskers");
        this.inlineMaskers = Objects.requireNonNull(inlineMaskers, "inlineMaskers");
    }

    @Override
    public @Nullable JsonNode applyMasking(@Nullable JsonNode data) throws Exception {
        return transformRecursive(data);
    }

    public @NonNull String maskJsonString(@NonNull String json) throws Exception {
        return applyMasking(new ObjectMapper().readTree(json)).toString();
    }

    public @NonNull String maskJsonPrettyString(@NonNull String json) throws Exception {
        return applyMasking(new ObjectMapper().readTree(json)).toPrettyString();
    }

    private @Nullable JsonNode transformRecursive(@Nullable JsonNode node) throws Exception {
        if (node == null) {
            return null;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode transformed = applyFieldMaskers(entry);
                if (transformed == entry.getValue()
                        && entry.getValue().isArray()
                        && !(entry.getValue().isEmpty())
                        && !(entry.getValue().get(0).isObject() || entry.getValue().get(0).isArray())
                ) {
                    // Special treatment for array values that weren't transformed yet
                    // and are no objects or arrays
                    ArrayNode arrayNode = (ArrayNode) entry.getValue();
                    for (int i = 0; i < arrayNode.size(); i++) {
                        JsonNode innerItem = arrayNode.get(i);
                        AbstractMap.SimpleEntry<String, JsonNode> synthEntry = new AbstractMap.SimpleEntry<>(
                                entry.getKey(),
                                innerItem
                        );
                        JsonNode innerTransformed = applyFieldMaskers(synthEntry);
                        if (innerTransformed != innerItem) {
                            arrayNode.set(i, innerTransformed);
                        }
                    }
                }

                if (transformed != entry.getValue()) {
                    // Transformation was applied, no further processing required
                    objectNode.set(entry.getKey(), transformed);
                } else {
                    // Recursive transformation
                    transformed = transformRecursive(entry.getValue());
                    if (transformed != entry.getValue()) {
                        objectNode.set(entry.getKey(), transformed);
                    }
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode item = arrayNode.get(i);
                JsonNode transformed = transformRecursive(item);
                if (transformed != item) {
                    arrayNode.set(i, transformed);
                }
            }
        } else if (node.isTextual()) {
            String original = node.asText();
            String replacement;
            for (Masker<String, String> m : inlineMaskers) {
                replacement = m.applyMasking(original);
                if (!(Objects.equals(original, replacement))) {
                    return new TextNode(replacement);
                }
            }
        }

        return node;
    }

    private JsonNode applyFieldMaskers(Map.Entry<String, JsonNode> entry) throws Exception {
        JsonNode transformed;
        for (Masker<Map.Entry<String, JsonNode>, JsonNode> m : fieldMaskers) {
            transformed = m.applyMasking(entry);
            if (transformed != entry.getValue()) {
                return transformed;
            }
        }
        return entry.getValue();
    }
}