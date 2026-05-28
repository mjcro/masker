/**
 * Auto-detecting body masker that picks between JSON, XML and
 * {@code application/x-www-form-urlencoded} maskers based on the shape of the
 * input string.
 *
 * <h2>Entry point</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.auto.AutoBodyMasker} &mdash; accepts a
 *       raw {@link java.lang.String} body and returns the masked string.
 *       Assembled from a {@link io.github.mjcro.masker.rules.Rulebook} via
 *       {@code usingRulebook(...)}, or composed manually from pre-built
 *       sub-maskers.</li>
 * </ul>
 *
 * <h2>Detection</h2>
 *
 * <p>The first non-whitespace character of the input decides the route:
 * <ul>
 *   <li>{@code '{'} or {@code '['} &rarr;
 *       {@link io.github.mjcro.masker.jackson.JsonNodeDocumentMasker}</li>
 *   <li>{@code '<'} &rarr;
 *       {@link io.github.mjcro.masker.xml.XmlStringStaxMasker}</li>
 *   <li>everything else &rarr;
 *       {@link io.github.mjcro.masker.formdata.FormDataStringMasker}</li>
 * </ul>
 *
 * <p>Detection is intentionally cheap and does not validate the payload. A
 * payload whose first character claims JSON or XML but whose body is malformed
 * surfaces as an exception thrown by the underlying masker.
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>{@code null}, empty and all-whitespace inputs pass through unchanged.</li>
 *   <li>Reference equality is preserved only for the form-data branch; the JSON
 *       and XML branches always re-serialize, so the returned string is a fresh
 *       instance even when no value was rewritten.</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 *
 * <p>Transitively requires {@code jackson-databind} (for the JSON branch) and a
 * JAXP-compatible StAX implementation (for the XML branch). Both are inherited
 * from the underlying maskers.
 */
package io.github.mjcro.masker.auto;
