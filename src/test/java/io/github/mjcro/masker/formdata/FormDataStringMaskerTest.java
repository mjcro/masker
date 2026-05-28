package io.github.mjcro.masker.formdata;

import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;
import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class FormDataStringMaskerTest {
    /**
     * Both the deprecated default rulebook and its builder-composed equivalent
     * must produce identical form-data masking output on every case in this class.
     */
    @SuppressWarnings("deprecation")
    static Stream<FormDataStringMasker> maskers() {
        Rulebook deprecated = new DefaultObjectFieldsRulebook();
        Rulebook builderBased = Rulebook.builder()
                .withMaskedCardData()
                .withMaskedIdentity()
                .withMaskedContacts()
                .withMaskedCredentials()
                .withMaskedIban()
                .withLongValueTruncation()
                .build();
        return Stream.of(
                FormDataStringMasker.usingRulebook(deprecated),
                FormDataStringMasker.usingRulebook(builderBased)
        );
    }

    private static Stream<Arguments> formDataCases() {
        List<Arguments> cases = List.of(
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

        return maskers().flatMap(m -> cases.stream().map(args -> {
            Object[] original = args.get();
            return Arguments.of(m, original[0], original[1]);
        }));
    }

    @ParameterizedTest(name = "{1} -> {2}")
    @MethodSource("formDataCases")
    void testFormDataMasking(FormDataStringMasker masker, String input, String expected) throws Exception {
        Assertions.assertEquals(expected, masker.applyMasking(input));
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void testNullAndEmptyInput(FormDataStringMasker masker) throws Exception {
        Assertions.assertNull(masker.applyMasking(null));
        Assertions.assertEquals("", masker.applyMasking(""));
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void testUnchangedReturnsSameReference(FormDataStringMasker masker) throws Exception {
        String input = "grant_type=client_credentials&scope=payments%3Aread+payments%3Awrite+providers%3Aread";
        Assertions.assertSame(input, masker.applyMasking(input));
    }
}
