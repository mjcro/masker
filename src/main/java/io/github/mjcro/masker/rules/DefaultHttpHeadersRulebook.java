package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Default rulebook settings to mask HTTP headers.
 */
public class DefaultHttpHeadersRulebook implements Rulebook {
    @Override
    public @Nullable Masker<String, String> getDefaultMasker() {
        return new StringLongTruncationMasker(64);
    }

    @Override
    public @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameEqualsMaskers() {
        return List.of(
                Rulebook.tuple(StringAuthorizationHeaderMasker.DEFAULT, "authorization"),
                Rulebook.tuple(StringUrlRequestUriMasker.DEFAULT, "referer"),
                Rulebook.tuple(
                        StringSmartLengthMasker.DEFAULT,
                        "proxy-authorization", "cookie", "set-cookie",
                        "token", "signature", "consent", "x-token", "x-auth-token", "x-api-key", "x-key",
                        "www-authenticate", "x-itc-token"
                )
        );
    }
}
