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
 * <h2>Building custom rules</h2>
 *
 * <p>The recommended entry point is {@link io.github.mjcro.masker.rules.Rulebook#builder()}.
 * The builder exposes granular withers for individual name-equals / name-contains /
 * inline rules, plus coarse bundle withers ({@code withMaskedCardData}, {@code withMaskedIdentity},
 * {@code withMaskedContacts}, {@code withMaskedCredentials}, {@code withMaskedHeaderCredentials},
 * {@code withMaskedIban}, {@code withLongValueTruncation}) that install opinionated rule
 * clusters for common data families. Bundles are additive and may be combined
 * freely with each other and with the low-level withers.
 *
 * <h2>Shipped implementations</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.rules.SimpleRulebook} — immutable
 *       container for custom rules; produced by the builder.</li>
 *   <li>{@link io.github.mjcro.masker.rules.RulebookBuilder} — fluent builder.</li>
 * </ul>
 */
package io.github.mjcro.masker.rules;
