package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringUrlRequestUriMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("g***m", "google.com"),
                Arguments.of("http://google.com", "http://google.com"),
                Arguments.of("http://google.com/", "http://google.com/"),
                Arguments.of("http://google.com/***", "http://google.com/a"),
                Arguments.of("http://google.com/***", "http://google.com/ab"),
                Arguments.of("https://google.com", "https://google.com"),
                Arguments.of("https://google.com/***", "https://google.com/a"),
                Arguments.of("https://google.com/***", "https://google.com/ab")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, CharSequence given) {
        Assertions.assertEquals(expected, StringUrlRequestUriMasker.DEFAULT.applyMasking(given));
    }
}