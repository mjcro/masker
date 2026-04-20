/**
 * {@link io.github.mjcro.masker.rules.Rulebook} contract and shipped default
 * implementations.
 *
 * <h2>Contract</h2>
 *
 * <p>A rulebook is a configuration bundle consumed by document-level maskers. It
 * exposes four independent, optional rule categories plus a charset:
 * <ul>
 *   <li>{@link io.github.mjcro.masker.rules.Rulebook#getNameEqualsMaskers()} —
 *       maskers triggered by case-insensitive exact name match.</li>
 *   <li>{@link io.github.mjcro.masker.rules.Rulebook#getNameContainsMaskers()} —
 *       maskers triggered when the name contains a configured substring.</li>
 *   <li>{@link io.github.mjcro.masker.rules.Rulebook#getInlineMaskers()} —
 *       maskers applied unconditionally to every textual leaf.</li>
 *   <li>{@link io.github.mjcro.masker.rules.Rulebook#getDefaultMasker()} —
 *       fallback masker when no other rule matches.</li>
 * </ul>
 *
 * <p>Document-level maskers document which categories they honour; rulebook
 * implementations are allowed to expose only a subset.
 *
 * <h2>Shipped implementations</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.rules.SimpleRulebook} — immutable
 *       container for custom rules; no subclassing required.</li>
 *   <li>{@link io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook} —
 *       opinionated defaults for JSON/XML payloads (card, IBAN, email, phone,
 *       names, credentials, ...).</li>
 *   <li>{@link io.github.mjcro.masker.rules.DefaultHttpHeadersRulebook} —
 *       opinionated defaults for HTTP headers ({@code Authorization},
 *       {@code Cookie}, {@code X-Api-Key}, ...).</li>
 * </ul>
 *
 * <h2>Building custom rules</h2>
 *
 * <p>Use {@link io.github.mjcro.masker.rules.Rulebook#tuple(io.github.mjcro.masker.Masker, String...)}
 * to pair a masker with the names (or substrings) that should trigger it, then
 * pass the list to {@code SimpleRulebook}.
 */
package io.github.mjcro.masker.rules;
