package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.mjcro.masker.strings.StringFullMasker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

public class JsonNodeTextObjectFieldMaskerTest {
    @Test
    void testMasking() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree("{\"one\":\"foo\",\"two\":\"foo\"}");
        JsonNodeTextObjectFieldMasker masker = new JsonNodeTextObjectFieldMasker(
                "two"::equalsIgnoreCase,
                StringFullMasker.DEFAULT
        );

        ObjectNode objectNode = (ObjectNode) tree;
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        Assertions.assertEquals(
                "foo",
                masker.applyMasking(fields.next()).asText()
        );
        Assertions.assertEquals(
                "***",
                masker.applyMasking(fields.next()).asText()
        );
    }
}