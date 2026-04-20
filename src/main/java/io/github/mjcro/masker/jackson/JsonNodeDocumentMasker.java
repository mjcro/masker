package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
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

/**
 * Masks a Jackson {@link JsonNode} tree in place.
 * The walker applies field-name maskers to every object field (both exact name and
 * {@code parent.child} compound name are tried) and inline maskers to every textual leaf.
 * Arrays of primitives inherit their parent field name so that e.g. {@code {"emails": ["a@b"]}}
 * is masked as if each element were named {@code emails}.
 * <p>
 * Not thread-safe for concurrent masking of the same node tree.
 * Callers who must keep the original document must pass a deep copy, since the node is mutated.
 */
public class JsonNodeDocumentMasker implements Masker<JsonNode, JsonNode> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<Masker<Map.Entry<String, JsonNode>, JsonNode>> fieldMaskers;
    private final List<Masker<String, String>> inlineMaskers;

    /**
     * Assembles a document masker from the given rulebook.
     * Name-equals and name-contains maskers become field-level maskers, inline maskers
     * are forwarded as-is.
     *
     * @param rulebook Non-null rulebook describing which fields to mask and how.
     * @return Ready-to-use document masker.
     */
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

    /**
     * Constructs new document masker.
     *
     * @param fieldMaskers  Non-null list of maskers accepting (name, node) entries.
     * @param inlineMaskers Non-null list of maskers applied to every textual leaf.
     */
    public JsonNodeDocumentMasker(
            @NonNull List<Masker<Map.Entry<String, JsonNode>, JsonNode>> fieldMaskers,
            @NonNull List<Masker<String, String>> inlineMaskers
    ) {
        this.fieldMaskers = Objects.requireNonNull(fieldMaskers, "fieldMaskers");
        this.inlineMaskers = Objects.requireNonNull(inlineMaskers, "inlineMaskers");
    }

    /**
     * Mutates the input node tree in place. Returns the same reference for non-null input.
     *
     * @param data Nullable root node.
     * @return Same {@code data} reference when non-null, {@code null} otherwise.
     * @throws Exception Propagated from underlying maskers.
     */
    @Override
    public @Nullable JsonNode applyMasking(@Nullable JsonNode data) throws Exception {
        return transformRecursive(data, null);
    }

    /**
     * Parses the given JSON string, masks it and serializes the result back.
     *
     * @param json Non-null, valid JSON string.
     * @return Non-null JSON string with sensitive fields masked.
     * @throws Exception When the input cannot be parsed or a masker fails.
     */
    public @NonNull String maskJsonString(@NonNull String json) throws Exception {
        return applyMasking(OBJECT_MAPPER.readTree(json)).toString();
    }

    /**
     * Same as {@link #maskJsonString(String)} but returns pretty-printed output.
     *
     * @param json Non-null, valid JSON string.
     * @return Non-null pretty-printed masked JSON.
     * @throws Exception When the input cannot be parsed or a masker fails.
     */
    public @NonNull String maskJsonPrettyString(@NonNull String json) throws Exception {
        return applyMasking(OBJECT_MAPPER.readTree(json)).toPrettyString();
    }

    private @Nullable JsonNode transformRecursive(@Nullable JsonNode node, @Nullable String parentName) throws Exception {
        if (node == null) {
            return null;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode transformed = applyFieldMaskers(entry, parentName);
                if (transformed != entry.getValue()) {
                    // Transformation was applied, no further processing required
                    objectNode.set(entry.getKey(), transformed);
                    continue;
                }

                if (entry.getValue().isArray()
                        && !(entry.getValue().isEmpty())
                        && !(entry.getValue().get(0).isObject() || entry.getValue().get(0).isArray())
                ) {
                    // Special treatment for array values that weren't transformed yet
                    // and are no objects or arrays
                    ArrayNode arrayNode = (ArrayNode) entry.getValue();
                    boolean anyInnerTransformed = false;
                    for (int i = 0; i < arrayNode.size(); i++) {
                        JsonNode innerItem = arrayNode.get(i);
                        AbstractMap.SimpleEntry<String, JsonNode> synthEntry = new AbstractMap.SimpleEntry<>(
                                entry.getKey(),
                                innerItem
                        );
                        JsonNode innerTransformed = applyFieldMaskers(synthEntry, parentName);
                        if (innerTransformed != innerItem) {
                            arrayNode.set(i, innerTransformed);
                            anyInnerTransformed = true;
                        }
                    }
                    // Skip fallback recursion — inline maskers would re-mask already-masked values.
                    if (anyInnerTransformed) {
                        continue;
                    }
                }

                // Recursive transformation
                transformed = transformRecursive(entry.getValue(), entry.getKey());
                if (transformed != entry.getValue()) {
                    objectNode.set(entry.getKey(), transformed);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode item = arrayNode.get(i);
                JsonNode transformed = transformRecursive(item, parentName);
                if (transformed != item) {
                    arrayNode.set(i, transformed);
                }
            }
        } else if (node.isTextual()) {
            final String original = node.asText();
            for (Masker<String, String> m : inlineMaskers) {
                final String replacement = m.applyMasking(original);
                if (!Objects.equals(original, replacement)) {
                    return replacement == null ? NullNode.getInstance() : new TextNode(replacement);
                }
            }
        }

        return node;
    }

    private JsonNode applyFieldMaskers(Map.Entry<String, JsonNode> entry, @Nullable String parentName) throws Exception {
        Map.Entry<String, JsonNode> compoundEntry = parentName == null
                ? null
                : new AbstractMap.SimpleEntry<>(parentName + "." + entry.getKey(), entry.getValue());
        for (Masker<Map.Entry<String, JsonNode>, JsonNode> m : fieldMaskers) {
            JsonNode transformed = m.applyMasking(entry);
            if (transformed != entry.getValue()) {
                return transformed;
            }
            if (compoundEntry != null) {
                transformed = m.applyMasking(compoundEntry);
                if (transformed != entry.getValue()) {
                    return transformed;
                }
            }
        }
        return entry.getValue();
    }
}