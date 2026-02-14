package io.github.mjcro.masker.headers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

public class DefaultHeadersMaskerTest {
    @Test
    void testHeadersMasking() throws Exception {
        LinkedHashMap<String, List<String>> given = new LinkedHashMap<>();
        given.put("Authorization", List.of("BASIC 234y2306298734692834", "BEARER dSifohbsd0f89sd8f76"));
        given.put("Token", List.of("HHLASJhda89ashLKJHASMB"));
        given.put("Consent", List.of("hh0087y34hbkjhlkljkdsfh"));
        given.put("Signature", List.of("28723hlkjhdfshdvgkhs8sdafs"));
        given.put("Referer", List.of("https://google.com/some-secret"));

        LinkedHashMap<String, List<String>> expected = new LinkedHashMap<>();
        expected.put("Authorization", List.of("BASIC 234***", "BEARER dSi***"));
        expected.put("Token", List.of("HH***B"));
        expected.put("Consent", List.of("hh***h"));
        expected.put("Signature", List.of("28***s"));
        expected.put("Referer", List.of("https://google.com/***"));

        Assertions.assertEquals(
                expected,
                new DefaultHeadersMasker().applyMasking(given)
        );
    }
}