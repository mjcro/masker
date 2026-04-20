package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;
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
    @Test
    void testMasking() throws Exception {
        String given = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-given.xml").toURI()));
        String expected = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-expected.xml").toURI()));

        System.out.println("Factory Class: " + XMLOutputFactory.newInstance().getClass().getName());
        Assertions.assertEquals(
                expected,
                XmlStringStaxMasker.usingRulebook(new DefaultObjectFieldsRulebook()).applyMasking(given)
        );
    }

    private static final String DECL = "<?xml version='1.0' encoding='UTF-8'?>";

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // Leaf element matching
                Arguments.of("<r><cvv>1234</cvv></r>", DECL + "<r><cvv>***</cvv></r>"),
                Arguments.of("<r><iban>UA1234561111119876</iban></r>", DECL + "<r><iban>UA12***9876</iban></r>"),
                Arguments.of("<r><first_name>Alexander</first_name></r>", DECL + "<r><first_name>A***</first_name></r>"),
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
                XmlStringStaxMasker.usingRulebook(new DefaultObjectFieldsRulebook()).applyMasking(given)
        );
    }
}