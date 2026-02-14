package io.github.mjcro.masker.headers;

import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;

import java.util.Map;

public class DefaultHeadersMasker extends HeadersPerNameMasker {
    public DefaultHeadersMasker() {
        super(Map.ofEntries(
                        Map.entry("authorization", StringAuthorizationHeaderMasker.DEFAULT),
                        Map.entry("token", StringSmartLengthMasker.DEFAULT),
                        Map.entry("proxy-authorization", StringSmartLengthMasker.DEFAULT),
                        Map.entry("cookie", StringSmartLengthMasker.DEFAULT),
                        Map.entry("set-cookie", StringSmartLengthMasker.DEFAULT),
                        Map.entry("consent", StringSmartLengthMasker.DEFAULT),
                        Map.entry("signature", StringSmartLengthMasker.DEFAULT),
                        Map.entry("www-authenticate", StringSmartLengthMasker.DEFAULT),
                        Map.entry("x-auth-token", StringSmartLengthMasker.DEFAULT),
                        Map.entry("x-api-key", StringSmartLengthMasker.DEFAULT),
                        Map.entry("x-token", StringSmartLengthMasker.DEFAULT),
                        Map.entry("x-key", StringSmartLengthMasker.DEFAULT),
                        Map.entry("x-itc-token", StringSmartLengthMasker.DEFAULT),
                        Map.entry("referer", StringUrlRequestUriMasker.DEFAULT)
                ),
                new StringLongTruncationMasker(64)
        );
    }
}