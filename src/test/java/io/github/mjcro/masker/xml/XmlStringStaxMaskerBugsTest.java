package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;
import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class XmlStringStaxMaskerBugsTest {
    private static final String DECL = "<?xml version='1.0' encoding='UTF-8'?>";

    /**
     * Both the deprecated default rulebook and its builder-composed equivalent
     * must exhibit the same CDATA/attribute handling behaviour.
     */
    @SuppressWarnings("deprecation")
    static Stream<XmlStringStaxMasker> maskers() {
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
                XmlStringStaxMasker.usingRulebook(deprecated),
                XmlStringStaxMasker.usingRulebook(builderBased)
        );
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void x1_cvvInsideCdataIsMasked(XmlStringStaxMasker masker) throws Exception {
        Assertions.assertEquals(
                DECL + "<r><cvv><![CDATA[***]]></cvv></r>",
                masker.applyMasking("<r><cvv><![CDATA[1234]]></cvv></r>")
        );
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void x1_panInsideCdataIsMaskedByInlineMasker(XmlStringStaxMasker masker) throws Exception {
        Assertions.assertEquals(
                DECL + "<r><free><![CDATA[***4444]]></free></r>",
                masker.applyMasking("<r><free><![CDATA[4111222233334444]]></free></r>")
        );
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void x2_namespacedAttributePreservesPrefix(XmlStringStaxMasker masker) throws Exception {
        String out = masker.applyMasking("<r xmlns:x=\"urn:x\"><item x:cvv=\"1234\"/></r>");
        Assertions.assertTrue(
                out.contains("x:cvv=\"***\""),
                "prefixed attribute should keep its prefix after masking; got: " + out
        );
    }
}
