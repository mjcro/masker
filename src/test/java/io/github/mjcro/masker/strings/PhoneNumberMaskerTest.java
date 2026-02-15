package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class PhoneNumberMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("***", "a"),
                Arguments.of("***", "foo"),
                Arguments.of("***", "12345"),
                Arguments.of("***", "123456"),
                Arguments.of("***67", "1234567"),
                Arguments.of("***78", "12345678")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, String given) {
        Assertions.assertEquals(expected, PhoneNumberMasker.DEFAULT.applyMasking(given));
    }
}