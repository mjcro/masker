package io.github.mjcro.masker.jackson;

import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JsonNodeDocumentMaskerTest {
    @Test
    void testMasking() throws Exception {
        String given = Files.readString(Path.of(getClass().getClassLoader().getResource("json-given.json").toURI()));
        String expected = Files.readString(Path.of(getClass().getClassLoader().getResource("json-expected.json").toURI()));

        Assertions.assertEquals(
                expected,
                JsonNodeDocumentMasker.usingRulebook(new DefaultObjectFieldsRulebook()).maskJsonPrettyString(given)
        );
    }

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // Object keys
                Arguments.of("{\"firstNAME\":\"Alexander\"}", "{\"firstNAME\":\"A***\"}"),
                Arguments.of("{\"FIRST_name\":\"Alexander\"}", "{\"FIRST_name\":\"A***\"}"),
                Arguments.of("{\"laSTname\":\"Richardson\"}", "{\"laSTname\":\"R***n\"}"),
                Arguments.of("{\"last_NAME\":\"Richardson\"}", "{\"last_NAME\":\"R***n\"}"),
                Arguments.of("{\"Login\":\"abcdef\"}", "{\"Login\":\"a***\"}"),
                Arguments.of("{\"merchant_login\":\"abcdef\"}", "{\"merchant_login\":\"a***\"}"),
                Arguments.of("{\"Password\":\"abcdef\"}", "{\"Password\":\"a***\"}"),
                Arguments.of("{\"merchant_password\":\"abcdef\"}", "{\"merchant_password\":\"a***\"}"),
                Arguments.of("{\"cVv\":\"1234\"}", "{\"cVv\":\"***\"}"),
                Arguments.of("{\"CVc\":\"1234\"}", "{\"CVc\":\"***\"}"),
                Arguments.of("{\"email\":\"theverylongemail@gmail.com\"}", "{\"email\":\"t***l@gmail.com\"}"),
                Arguments.of("{\"pan\":\"1234561111119876\"}", "{\"pan\":\"***9876\"}"),
                Arguments.of("{\"iban\":\"UA1234561111119876\"}", "{\"iban\":\"UA12***9876\"}"),
                // Inline
                Arguments.of("\"1234561111119876\"", "\"***9876\"")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMasking(final String given, final String expected) throws Exception {
        Assertions.assertEquals(
                expected,
                JsonNodeDocumentMasker.usingRulebook(new DefaultObjectFieldsRulebook()).maskJsonString(given)
        );
    }
}