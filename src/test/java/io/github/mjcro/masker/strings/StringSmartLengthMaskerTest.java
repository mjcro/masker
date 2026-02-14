package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringSmartLengthMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("     ", "     "),
                Arguments.of(" \t\n\r\f", " \t\n\r\f"),
                Arguments.of("***", "a"),
                Arguments.of("***", "foo"),
                Arguments.of("1***", "1234"),
                Arguments.of("1***", "12345"),
                Arguments.of("1***", "123456"),
                Arguments.of("1***", "1234567"),
                Arguments.of("1***", "123456789"),
                Arguments.of("1***0", "1234567890"),
                Arguments.of("12***0", "12345678901234567890"),
                Arguments.of("1234***0", "1234567890123456789012345678901234567890")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, CharSequence given) {
        Assertions.assertEquals(expected, StringSmartLengthMasker.DEFAULT.applyMasking(given));
    }
}