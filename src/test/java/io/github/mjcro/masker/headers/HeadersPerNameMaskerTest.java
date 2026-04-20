package io.github.mjcro.masker.headers;

import io.github.mjcro.masker.rules.DefaultHttpHeadersRulebook;
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HeadersPerNameMaskerTest {
    /**
     * Both the deprecated default rulebook and its builder-composed equivalent
     * must produce identical header masking output on every case in this class.
     */
    @SuppressWarnings("deprecation")
    static Stream<HeadersPerNameMasker> maskers() {
        Rulebook deprecated = new DefaultHttpHeadersRulebook();
        Rulebook builderBased = Rulebook.builder()
                .withMaskedHeaderCredentials()
                .withDefaultMasker(new StringLongTruncationMasker(64))
                .build();
        return Stream.of(
                HeadersPerNameMasker.usingRulebook(deprecated),
                HeadersPerNameMasker.usingRulebook(builderBased)
        );
    }

    private static Stream<Arguments> headerMaskingCases() {
        String longValue = "a".repeat(70);
        String longExpected = "a".repeat(52) + "[...20...]" + "aa";

        List<Arguments> cases = List.of(
                // StringAuthorizationHeaderMasker
                Arguments.of("Authorization", "BASIC 234y2306298734692834", "BASIC 234***"),
                Arguments.of("Authorization", "BEARER dSifohbsd0f89sd8f76", "BEARER dSi***"),
                // StringUrlRequestUriMasker
                Arguments.of("Referer", "https://google.com/some-secret", "https://google.com/***"),
                // StringSmartLengthMasker name-equals entries
                Arguments.of("Token", "HHLASJhda89ashLKJHASMB", "HH***B"),
                Arguments.of("ConsentToken", "9-8ahdashdahsgda76agsdaska", "9-***a"),
                Arguments.of("Consent", "hh0087y34hbkjhlkljkdsfh", "hh***h"),
                Arguments.of("Signature", "28723hlkjhdfshdvgkhs8sdafs", "28***s"),
                Arguments.of("Proxy-Authorization", "BASIC dXNlcjpwYXNzd29yZA==", "BA***="),
                Arguments.of("Cookie", "sessionid=abc123xyz789def", "se***f"),
                Arguments.of("Set-Cookie", "session=xyz12345; Path=/", "se***/"),
                Arguments.of("X-Token", "tok_abcdef12345", "t***5"),
                Arguments.of("X-Auth-Token", "auth_xyz98765", "a***5"),
                Arguments.of("X-Api-Key", "sk_live_abcdef123456", "sk***6"),
                Arguments.of("X-Key", "keyvalue123", "k***3"),
                Arguments.of("WWW-Authenticate", "Bearer realm=example", "Be***e"),
                Arguments.of("X-Itc-Token", "itc_token_secret", "i***t"),
                Arguments.of("Consent-Token", "ctvalue98765abcd", "c***d"),
                // Default masker: StringLongTruncationMasker(64) truncates over-threshold values
                Arguments.of("X-Long-Header", longValue, longExpected)
        );

        return maskers().flatMap(m -> cases.stream().map(args -> {
            Object[] original = args.get();
            return Arguments.of(m, original[0], original[1], original[2]);
        }));
    }

    @ParameterizedTest(name = "{1}: {2} -> {3}")
    @MethodSource("headerMaskingCases")
    void testHeaderMasking(HeadersPerNameMasker masker, String name, String input, String expected) throws Exception {
        Assertions.assertEquals(
                Map.of(name, List.of(expected)),
                masker.applyMasking(Map.of(name, List.of(input)))
        );
    }

    private static Stream<Arguments> headersLeftIntactCases() {
        List<Arguments> cases = List.of(
                Arguments.of("Content-Type", "application/json"),
                Arguments.of("Accept", "*/*"),
                Arguments.of("Accept-Encoding", "gzip, deflate, br"),
                Arguments.of("Accept-Language", "en-US,en;q=0.9"),
                Arguments.of("User-Agent", "curl/7.81.0"),
                Arguments.of("Host", "example.com"),
                Arguments.of("Content-Length", "1234"),
                Arguments.of("Connection", "keep-alive"),
                Arguments.of("Cache-Control", "no-cache"),
                Arguments.of("Date", "Wed, 21 Oct 2015 07:28:00 GMT"),
                Arguments.of("ETag", "\"33a64df551425fcc55e4d42a148795d9f25f89d4\""),
                Arguments.of("X-Request-Id", "req-abc-123"),
                Arguments.of("X-Forwarded-For", "203.0.113.1")
        );

        return maskers().flatMap(m -> cases.stream().map(args -> {
            Object[] original = args.get();
            return Arguments.of(m, original[0], original[1]);
        }));
    }

    @ParameterizedTest(name = "{1}: {2} (unchanged)")
    @MethodSource("headersLeftIntactCases")
    void testHeadersLeftIntact(HeadersPerNameMasker masker, String name, String value) throws Exception {
        Map<String, List<String>> given = Map.of(name, List.of(value));
        Assertions.assertEquals(given, masker.applyMasking(given));
    }

    @ParameterizedTest
    @MethodSource("maskers")
    void testNullAndEmptyInput(HeadersPerNameMasker masker) throws Exception {
        Assertions.assertNull(masker.applyMasking(null));
        Assertions.assertEquals(Collections.emptyMap(), masker.applyMasking(Collections.emptyMap()));
    }
}
