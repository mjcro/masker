/**
 * Internal utility predicates used by
 * {@link io.github.mjcro.masker.NameMatchingMaskerDecorator} and the header
 * masker to match field/header names in a case-insensitive manner.
 *
 * <h2>Types</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.util.EqualsCaseInsensitivePredicate} —
 *       matches when the tested string equals one of the configured values
 *       (lowercased with {@link java.util.Locale#ROOT}).</li>
 *   <li>{@link io.github.mjcro.masker.util.ContainsCaseInsensitivePredicate} —
 *       matches when the tested string contains one of the configured
 *       substrings.</li>
 * </ul>
 *
 * <p>Both predicates treat {@code null} input as a non-match.
 *
 * <p>These classes are part of the public API only because they are referenced
 * by other public types; most consumers should prefer
 * {@link io.github.mjcro.masker.NameMatchingMaskerDecorator#equalsCaseInsensitive(io.github.mjcro.masker.Masker, String...)}
 * and
 * {@link io.github.mjcro.masker.NameMatchingMaskerDecorator#containsCaseInsensitive(io.github.mjcro.masker.Masker, String...)}.
 */
package io.github.mjcro.masker.util;
