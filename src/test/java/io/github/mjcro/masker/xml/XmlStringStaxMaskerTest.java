package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLOutputFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class XmlStringStaxMaskerTest {
    private static final Rulebook RULEBOOK = Rulebook.builder()
            .withMaskedCardData()
            .withMaskedIdentity()
            .withMaskedContacts()
            .withMaskedCredentials()
            .withMaskedIban()
            .withLongValueTruncation()
            .build();

    @Test
    void testMasking() throws Exception {
        String given = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-given.xml").toURI()));
        String expected = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-expected.xml").toURI()));

        System.out.println("Factory Class: " + XMLOutputFactory.newInstance().getClass().getName());
        Assertions.assertEquals(
                expected,
                XmlStringStaxMasker.usingRulebook(RULEBOOK).applyMasking(given)
        );
    }

    private static final String DECL = "<?xml version='1.0' encoding='UTF-8'?>";

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // Leaf element matching
                Arguments.of("<r><cvv>1234</cvv></r>", DECL + "<r><cvv>***</cvv></r>"),
                Arguments.of("<r><cvv2>1234</cvv2></r>", DECL + "<r><cvv2>***</cvv2></r>"),
                Arguments.of("<r><cavv>AAABBwAgAABQBw==</cavv></r>", DECL + "<r><cavv>***</cavv></r>"),
                Arguments.of("<r><track2>;1234567890123456=1234?</track2></r>", DECL + "<r><track2>***</track2></r>"),
                Arguments.of("<r><networkToken>tok_abc</networkToken></r>", DECL + "<r><networkToken>***</networkToken></r>"),
                Arguments.of("<r><iban>UA1234561111119876</iban></r>", DECL + "<r><iban>UA12***9876</iban></r>"),
                Arguments.of("<r><first_name>Alexander</first_name></r>", DECL + "<r><first_name>A***</first_name></r>"),
                Arguments.of("<r><nameOnCard>John Smith</nameOnCard></r>", DECL + "<r><nameOnCard>J***h</nameOnCard></r>"),
                Arguments.of("<r><passportNumber>X1234567</passportNumber></r>", DECL + "<r><passportNumber>X***</passportNumber></r>"),
                Arguments.of("<r><tin>123-45-6789</tin></r>", DECL + "<r><tin>1***9</tin></r>"),
                Arguments.of("<r><addressLine1>221B Baker Street</addressLine1></r>", DECL + "<r><addressLine1>2***t</addressLine1></r>"),
                // Compound (parent.leaf) matching
                Arguments.of("<r><payer><name>Abraham Lincoln</name></payer></r>", DECL + "<r><payer><name>A***n</name></payer></r>"),
                Arguments.of("<r><payee><name>Abraham Lincoln</name></payee></r>", DECL + "<r><payee><name>A***n</name></payee></r>"),
                Arguments.of("<r><sender><name>Abraham Lincoln</name></sender></r>", DECL + "<r><sender><name>A***n</name></sender></r>"),
                Arguments.of("<r><recipient><name>Abraham Lincoln</name></recipient></r>", DECL + "<r><recipient><name>A***n</name></recipient></r>"),
                Arguments.of("<r><beneficiary><name>Abraham Lincoln</name></beneficiary></r>", DECL + "<r><beneficiary><name>A***n</name></beneficiary></r>"),
                // Case insensitivity
                Arguments.of("<r><PAYER><Name>Abraham Lincoln</Name></PAYER></r>", DECL + "<r><PAYER><Name>A***n</Name></PAYER></r>"),
                // Bare <name> or unknown parent stays unchanged
                Arguments.of("<r><name>Abraham Lincoln</name></r>", DECL + "<r><name>Abraham Lincoln</name></r>"),
                Arguments.of("<r><other><name>Abraham Lincoln</name></other></r>", DECL + "<r><other><name>Abraham Lincoln</name></other></r>"),
                // Sibling leaf after nested element — parent context preserved
                Arguments.of(
                        "<r><payer><name>Abraham Lincoln</name><cvv>1234</cvv></payer></r>",
                        DECL + "<r><payer><name>A***n</name><cvv>***</cvv></payer></r>"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(final String given, final String expected) throws Exception {
        Assertions.assertEquals(
                expected,
                XmlStringStaxMasker.usingRulebook(RULEBOOK).applyMasking(given)
        );
    }
}
