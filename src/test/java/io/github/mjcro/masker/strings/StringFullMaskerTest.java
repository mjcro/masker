package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringFullMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("     ", "     "),
                Arguments.of(" \t\n\r\f", " \t\n\r\f"),
                Arguments.of("***", "a"),
                Arguments.of("***", "foo")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, CharSequence given) {
        Assertions.assertEquals(expected, StringFullMasker.DEFAULT.applyMasking(given));
    }
}