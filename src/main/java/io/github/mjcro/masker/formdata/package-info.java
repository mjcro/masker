/**
 * Masker for {@code application/x-www-form-urlencoded} payloads.
 *
 * <h2>Entry point</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.formdata.FormDataStringMasker} — accepts a
 *       raw form-encoded {@link java.lang.String} and returns the same string with
 *       sensitive values masked. Assembled from a
 *       {@link io.github.mjcro.masker.rules.Rulebook} via
 *       {@code usingRulebook(...)}.</li>
 * </ul>
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>The input is split on {@code &}; each value is URL-decoded using the
 *       rulebook's {@link io.github.mjcro.masker.rules.Rulebook#getCharset()
 *       charset} before being shown to maskers, then URL-encoded back if it
 *       was rewritten.</li>
 *   <li>All four rulebook categories are honoured: name-equals and
 *       name-contains for key lookup, inline maskers as an unconditional pass
 *       on the decoded value, and the default masker as a final fallback.</li>
 *   <li>Key insertion order and duplicate keys are preserved.</li>
 *   <li>Pairs whose value did not change are emitted verbatim from the source
 *       string, preserving the original encoding form; rewritten pairs are
 *       re-encoded canonically.</li>
 *   <li>{@code null} and empty inputs pass through unchanged.</li>
 * </ul>
 */
package io.github.mjcro.masker.formdata;
