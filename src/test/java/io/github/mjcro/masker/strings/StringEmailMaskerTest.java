package io.github.mjcro.masker.strings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringEmailMaskerTest {
    static Stream<Arguments> dataProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("***", "a"),
                Arguments.of("***", "foo"),
                Arguments.of("***@bar.com", "foo@bar.com"),
                Arguments.of("f***@bar.com", "foofoo@bar.com"),
                Arguments.of("f***o@bar.com", "foofoofoofoofoofoo@bar.com")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testMasking(String expected, String given) {
        Assertions.assertEquals(expected, StringEmailMasker.DEFAULT.applyMasking(given));
    }
}