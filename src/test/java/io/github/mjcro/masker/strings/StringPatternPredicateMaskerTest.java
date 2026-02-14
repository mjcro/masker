package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringPatternPredicateMaskerTest {
    @Test
    void testPatternMatching() throws Exception {
        StringPatternPredicateMasker masker = StringPatternPredicateMasker.compile(
                "^[0-9]{3,}$",
                StringFullMasker.DEFAULT
        );
        Assertions.assertNull(masker.applyMasking(null));
        Assertions.assertEquals("1", masker.applyMasking("1"));
        Assertions.assertEquals("12", masker.applyMasking("12"));
        Assertions.assertEquals("***", masker.applyMasking("123"));
        Assertions.assertEquals("***", masker.applyMasking("1234"));
    }
}