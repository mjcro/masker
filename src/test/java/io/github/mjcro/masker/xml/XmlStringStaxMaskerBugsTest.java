package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XmlStringStaxMaskerBugsTest {
    private static final String DECL = "<?xml version='1.0' encoding='UTF-8'?>";

    private static final XmlStringStaxMasker MASKER = XmlStringStaxMasker.usingRulebook(
            Rulebook.builder()
                    .withMaskedCardData()
                    .withMaskedIdentity()
                    .withMaskedContacts()
                    .withMaskedCredentials()
                    .withMaskedIban()
                    .withLongValueTruncation()
                    .build()
    );

    @Test
    void x1_cvvInsideCdataIsMasked() throws Exception {
        Assertions.assertEquals(
                DECL + "<r><cvv><![CDATA[***]]></cvv></r>",
                MASKER.applyMasking("<r><cvv><![CDATA[1234]]></cvv></r>")
        );
    }

    @Test
    void x1_panInsideCdataIsMaskedByInlineMasker() throws Exception {
        Assertions.assertEquals(
                DECL + "<r><free><![CDATA[***4444]]></free></r>",
                MASKER.applyMasking("<r><free><![CDATA[4111222233334444]]></free></r>")
        );
    }

    @Test
    void x2_namespacedAttributePreservesPrefix() throws Exception {
        String out = MASKER.applyMasking("<r xmlns:x=\"urn:x\"><item x:cvv=\"1234\"/></r>");
        Assertions.assertTrue(
                out.contains("x:cvv=\"***\""),
                "prefixed attribute should keep its prefix after masking; got: " + out
        );
    }
}
