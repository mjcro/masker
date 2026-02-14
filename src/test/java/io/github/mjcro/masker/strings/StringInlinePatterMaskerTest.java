package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringInlinePatterMaskerTest {
    @Test
    void testInlineReplacement() throws Exception {
        String subject = "PAN is 1234123412341234 and CVV is 678.";

        Assertions.assertEquals(
                "PAN is ***1234 and CVV is 678.",
                StringInlinePatterMasker.compile(
                        "[0-9]{12,19}",
                        StringCardPanMasker.DEFAULT
                ).applyMasking(subject)
        );
        Assertions.assertEquals(
                "PAN is 1234123412341234 and CVV is ***.",
                StringInlinePatterMasker.compile(
                        "\\b([0-9]{3,4})\\b",
                        StringFullMasker.DEFAULT
                ).applyMasking(subject)
        );
    }

    @Test
    void testMultipleInlineOccurrences() throws Exception {
        String subject = "111 is first, 99999 not matched but 222 - second and the third is 1234";
        Assertions.assertEquals(
                "*** is first, 99999 not matched but *** - second and the third is ***",
                StringInlinePatterMasker.compile(
                        "\\b([0-9]{3,4})\\b",
                        StringFullMasker.DEFAULT
                ).applyMasking(subject)
        );
    }
}