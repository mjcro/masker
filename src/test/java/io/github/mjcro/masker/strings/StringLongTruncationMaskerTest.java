package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringLongTruncationMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("     ", "     "),
                Arguments.of(" \t\n\r\f", " \t\n\r\f"),
                Arguments.of("12345678901234567890123456789012", "12345678901234567890123456789012"),
                Arguments.of("12345678901234567890[...15...]23", "123456789012345678901234567890123"),
                Arguments.of("12345678901234567890[...16...]34", "1234567890123456789012345678901234"),
                Arguments.of("12345678901234567890[...22...]90", "1234567890123456789012345678901234567890")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, String given) {
        Assertions.assertEquals(expected, new StringLongTruncationMasker(32).applyMasking(given));
    }
}