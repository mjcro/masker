package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringIbanMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("UA12***", "UA1234567890"),
                Arguments.of("UA12***5678", "U A 1234 5678 9012 3456 78")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, CharSequence given) {
        Assertions.assertEquals(expected, StringIbanMasker.DEFAULT.applyMasking(given));
    }

    @Test
    void testInline() throws Exception {
        Assertions.assertEquals(
                "This is inline UA12*** IBAN with some extra text",
                StringIbanMasker.DEFAULT.asInlineMasker().applyMasking("This is inline UA12345678901234 IBAN with some extra text")
        );
    }
}