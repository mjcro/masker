package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringInlinePatterMaskerTest {
    @Test
    void testInlineReplacement() throws Exception {
        String subject = "PAN is 1234123412341234 and CVV is 678.";

        Assertions.assertEquals(
                "PAN is 123412***1234 and CVV is 678.",
                StringInlinePatterMasker.compile(
                        "([0-9]{12,19})",
                        StringCardPanMasker.DEFAULT
                ).applyMasking(subject)
        );
        Assertions.assertEquals(
                "PAN is 1234123412341234 and CVV is ***.",
                StringInlinePatterMasker.compile(
                        "\\W([0-9]{3,4})\\W",
                        StringFullMasker.DEFAULT
                ).applyMasking(subject)
        );

    }
}