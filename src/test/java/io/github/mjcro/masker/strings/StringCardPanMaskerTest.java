package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringCardPanMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("123456***5678", "1234567812345678"),
                Arguments.of("123456***5678", "1234 5678 1234 5678"),
                Arguments.of("123456***5678", "1234-5678-1234-5678")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, CharSequence given) {
        Assertions.assertEquals(expected, StringCardPanMasker.DEFAULT.applyMasking(given));
    }
}