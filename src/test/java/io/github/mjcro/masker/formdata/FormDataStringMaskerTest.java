package io.github.mjcro.masker.formdata;

import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class FormDataStringMaskerTest {
    private static final FormDataStringMasker MASKER = FormDataStringMasker.usingRulebook(
            Rulebook.builder()
                    .withMaskedCardData()
                    .withMaskedIdentity()
                    .withMaskedContacts()
                    .withMaskedCredentials()
                    .withMaskedIban()
                    .withLongValueTruncation()
                    .build()
    );

    private static Stream<Arguments> formDataCases() {
        return Stream.of(
                // OAuth token request — neither field nor any value matches a rule,
                // so the input must come back byte-for-byte (including `+` and %3A).
                Arguments.of(
                        "grant_type=client_credentials&scope=payments%3Aread+payments%3Awrite+providers%3Aread",
                        "grant_type=client_credentials&scope=payments%3Aread+payments%3Awrite+providers%3Aread"
                ),
                // name-contains "password" -> StringSmartLengthMasker
                Arguments.of("password=secret123456", "password=s***6"),
                // name-equals "card" -> StringCardPanMasker (keeps last 4)
                Arguments.of("card=4111111111111111", "card=***1111"),
                // name-equals "iban" -> StringIbanMasker (first 4 + last 4)
                Arguments.of("iban=GB82WEST12345698765432", "iban=GB82***5432"),
                // name-contains "email" -> local part masked, '@' re-encoded as %40
                Arguments.of("email=user@example.com", "email=u***%40example.com"),
                // Order preservation across mixed sensitive/insensitive fields
                Arguments.of(
                        "name=alice&password=secret123456&id=42&card=4111111111111111",
                        "name=alice&password=s***6&id=42&card=***1111"
                ),
                // Duplicate keys: both values must be masked, order preserved
                Arguments.of("password=alpha&password=bravo", "password=a***&password=b***"),
                // URL-encoded value: decoded "!@#secret" (9 chars) -> "!***" -> re-encoded
                Arguments.of("password=%21%40%23secret", "password=%21***"),
                // Bare flag without '=' is emitted verbatim, sibling masked
                Arguments.of("debug&password=secret123456", "debug&password=s***6"),
                // Inline pattern catches a 12-19 digit PAN sitting in a non-card field
                Arguments.of("note=4111111111111111", "note=***1111")
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("formDataCases")
    void testFormDataMasking(String input, String expected) throws Exception {
        Assertions.assertEquals(expected, MASKER.applyMasking(input));
    }

    @Test
    void testNullAndEmptyInput() throws Exception {
        Assertions.assertNull(MASKER.applyMasking(null));
        Assertions.assertEquals("", MASKER.applyMasking(""));
    }

    @Test
    void testUnchangedReturnsSameReference() throws Exception {
        String input = "grant_type=client_credentials&scope=payments%3Aread+payments%3Awrite+providers%3Aread";
        Assertions.assertSame(input, MASKER.applyMasking(input));
    }
}
