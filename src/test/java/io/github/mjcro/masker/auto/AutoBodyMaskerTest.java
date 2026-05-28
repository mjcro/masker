package io.github.mjcro.masker.auto;

import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AutoBodyMaskerTest {
    private static final String XML_DECL = "<?xml version='1.0' encoding='UTF-8'?>";

    private static final AutoBodyMasker MASKER = AutoBodyMasker.usingRulebook(
            Rulebook.builder()
                    .withMaskedCardData()
                    .withMaskedIdentity()
                    .withMaskedContacts()
                    .withMaskedCredentials()
                    .withMaskedIban()
                    .withLongValueTruncation()
                    .build()
    );

    private static Stream<Arguments> dispatchCases() {
        return Stream.of(
                // JSON object -> JsonNodeDocumentMasker
                Arguments.of(
                        "{\"card\":\"4111111111111111\",\"note\":\"hi\"}",
                        "{\"card\":\"***1111\",\"note\":\"hi\"}"
                ),
                // JSON with leading whitespace still routed as JSON
                Arguments.of(
                        "   \n{\"password\":\"secret123456\"}",
                        "{\"password\":\"s***6\"}"
                ),
                // JSON array root
                Arguments.of(
                        "[{\"iban\":\"GB82WEST12345698765432\"}]",
                        "[{\"iban\":\"GB82***5432\"}]"
                ),
                // XML -> XmlStringStaxMasker (prepends declaration via StAX writer)
                Arguments.of(
                        "<r><cvv>1234</cvv></r>",
                        XML_DECL + "<r><cvv>***</cvv></r>"
                ),
                // XML with leading whitespace still routed as XML
                Arguments.of(
                        "\t  <r><iban>UA1234561111119876</iban></r>",
                        XML_DECL + "<r><iban>UA12***9876</iban></r>"
                ),
                // form-data -> FormDataStringMasker
                Arguments.of(
                        "card=4111111111111111&note=hello",
                        "card=***1111&note=hello"
                ),
                // form-data without sensitive fields -> returned verbatim
                Arguments.of(
                        "grant_type=client_credentials&scope=read+write",
                        "grant_type=client_credentials&scope=read+write"
                )
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("dispatchCases")
    void testDispatch(String input, String expected) throws Exception {
        Assertions.assertEquals(expected, MASKER.applyMasking(input));
    }

    @Test
    void testNullInput() throws Exception {
        Assertions.assertNull(MASKER.applyMasking(null));
    }

    @Test
    void testEmptyInput() throws Exception {
        Assertions.assertEquals("", MASKER.applyMasking(""));
    }

    @Test
    void testBlankInput() throws Exception {
        String input = "   \t\n ";
        Assertions.assertSame(input, MASKER.applyMasking(input));
    }

    @Test
    void testFormDataUnchangedReturnsSameReference() throws Exception {
        String input = "grant_type=client_credentials&scope=read+write";
        Assertions.assertSame(input, MASKER.applyMasking(input));
    }
}
