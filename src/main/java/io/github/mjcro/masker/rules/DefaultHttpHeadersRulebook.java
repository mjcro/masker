package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;

import java.util.List;
import java.util.Map;

/**
 * Opinionated rulebook targeting HTTP headers: {@code Authorization}, {@code Proxy-Authorization},
 * {@code Cookie}/{@code Set-Cookie}, {@code Referer}, various {@code X-*-Token}/{@code X-Api-Key} variants
 * and consent tokens. A 64-character truncation masker is installed as default for any other header.
 * Name matches are case-insensitive exact match.
 *
 * @deprecated Prefer composing the equivalent configuration with {@link Rulebook#builder()},
 * which lets callers pick the subset of rules they actually need. The full
 * content of this rulebook is reproduced by:
 * <pre>{@code
 * Rulebook.builder()
 *     .withMaskedHeaderCredentials()
 *     .withDefaultMasker(new StringLongTruncationMasker(64))
 *     .build();
 * }</pre>
 * This class will be removed in a future release.
 */
@Deprecated
public class DefaultHttpHeadersRulebook extends SimpleRulebook {
    public static final List<Map.Entry<String[], Masker<String, String>>> NAME_EQ_MASKERS = List.of(
            Rulebook.tuple(StringAuthorizationHeaderMasker.DEFAULT, "authorization"),
            Rulebook.tuple(StringUrlRequestUriMasker.DEFAULT, "referer"),
            Rulebook.tuple(
                    StringSmartLengthMasker.DEFAULT,
                    "proxy-authorization", "cookie", "set-cookie",
                    "token", "signature", "consent", "x-token", "x-auth-token", "x-api-key", "x-key",
                    "www-authenticate", "x-itc-token", "consent-token", "consenttoken"
            )
    );

    /**
     * Constructs rulebook using UTF-8 charset and a 64-character truncation default masker.
     */
    public DefaultHttpHeadersRulebook() {
        super(
                null,
                new StringLongTruncationMasker(64),
                null,
                NAME_EQ_MASKERS,
                null
        );
    }
}
