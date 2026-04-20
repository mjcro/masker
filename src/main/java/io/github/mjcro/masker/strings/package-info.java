/**
 * Leaf {@link io.github.mjcro.masker.Masker} implementations operating on
 * {@link java.lang.String} values.
 *
 * <p>Each masker targets a single data shape and is safe to reuse across threads
 * once constructed. Most classes expose a preconfigured {@code DEFAULT} instance
 * that should be preferred over direct instantiation unless custom fallbacks are
 * required.
 *
 * <h2>Behaviour classes</h2>
 *
 * <h3>Full / length-based</h3>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.strings.StringFullMasker} — replaces the
 *       entire value with a fixed token ({@code ***}). {@code null} and blank
 *       inputs pass through unchanged.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringSmartLengthMasker} — keeps a
 *       short prefix and/or suffix whose size scales with the input length.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringLongTruncationMasker} — log
 *       size guard: shortens oversized strings with a
 *       {@code "[...N...]"} marker.</li>
 * </ul>
 *
 * <h3>Domain-specific</h3>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.strings.StringCardPanMasker} — card PAN,
 *       keeps the last four digits.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringIbanMasker} — IBAN, keeps
 *       the first four characters and, for long IBANs, the last four.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringEmailMasker} — masks the
 *       local part, preserves the domain.</li>
 *   <li>{@link io.github.mjcro.masker.strings.PhoneNumberMasker} — keeps the
 *       last two digits.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker} —
 *       HTTP {@code Authorization} style headers; keeps scheme plus a short
 *       credential prefix.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringUrlRequestUriMasker} —
 *       preserves scheme and host, masks the request URI path and query.</li>
 * </ul>
 *
 * <h3>Regex-driven</h3>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.strings.StringPatternPredicateMasker} —
 *       invokes the wrapped masker only when the whole input matches a regex.</li>
 *   <li>{@link io.github.mjcro.masker.strings.StringInlinePatternMasker} —
 *       finds every regex match inside the input and passes each match through
 *       the wrapped masker.</li>
 * </ul>
 *
 * <h2>Conventions</h2>
 * <ul>
 *   <li>All maskers return the <em>same</em> reference when the input is not
 *       modified; this is required by the reference-equality short-circuit used
 *       by composers and document-level maskers.</li>
 *   <li>Every masker accepts {@code null} and blank input and delegates those to
 *       its configured fallback masker (or returns them unchanged).</li>
 * </ul>
 */
package io.github.mjcro.masker.strings;
