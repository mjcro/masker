/**
 * Masker for HTTP header maps.
 *
 * <h2>Entry point</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.headers.HeadersPerNameMasker} — accepts a
 *       {@code Map<String, List<String>>} (the shape used by most HTTP client
 *       and server APIs) and returns a new {@link java.util.LinkedHashMap} with
 *       the same keys and masked values. Assembled from a
 *       {@link io.github.mjcro.masker.rules.Rulebook} via
 *       {@code usingRulebook(...)}.</li>
 * </ul>
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>Header name lookup is case-insensitive (names are lowercased with
 *       {@link java.util.Locale#ROOT}).</li>
 *   <li>Only {@link io.github.mjcro.masker.rules.Rulebook#getNameEqualsMaskers()}
 *       and {@link io.github.mjcro.masker.rules.Rulebook#getDefaultMasker()}
 *       are consumed; other rule categories are ignored.</li>
 *   <li>{@code null} and empty input maps pass through unchanged.</li>
 *   <li>Insertion order of the input map is preserved in the output.</li>
 * </ul>
 */
package io.github.mjcro.masker.headers;
