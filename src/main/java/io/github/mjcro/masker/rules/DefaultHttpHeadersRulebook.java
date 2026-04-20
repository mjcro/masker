package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;

import java.util.List;
import java.util.Map;

/**
 * Default rulebook settings to mask HTTP headers.
 */
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
