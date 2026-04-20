/**
 * Document-level masker for Jackson {@code JsonNode} trees.
 *
 * <h2>Entry point</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.jackson.JsonNodeDocumentMasker} — assembles
 *       from a {@link io.github.mjcro.masker.rules.Rulebook} via
 *       {@code usingRulebook(...)} and exposes
 *       {@code applyMasking(JsonNode)}, {@code maskJsonString(String)} and
 *       {@code maskJsonPrettyString(String)}.</li>
 *   <li>{@link io.github.mjcro.masker.jackson.JsonNodeTextualMaskerDecorator} —
 *       adapter that applies a {@code String} masker to the textual content of
 *       a {@code JsonNode} and preserves reference equality for unchanged
 *       values.</li>
 * </ul>
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>Mutates the input tree <em>in place</em>; callers who must retain the
 *       original should pass a deep copy.</li>
 *   <li>For every object field both {@code name} and
 *       {@code parent.name} are tried against the field-name maskers, allowing
 *       compound rules such as {@code payer.name}.</li>
 *   <li>Arrays of primitives inherit the parent field name; e.g.
 *       {@code {"emails": ["a@b", ...]}} is masked as if each item were named
 *       {@code emails}.</li>
 *   <li>Inline maskers are applied to every textual leaf that was not already
 *       rewritten by a field-name masker.</li>
 *   <li>Modification is detected via reference equality, so leaf maskers must
 *       return the same instance when they perform no change.</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 *
 * <p>Requires {@code jackson-databind} on the classpath (declared with
 * {@code provided} scope by this library).
 */
package io.github.mjcro.masker.jackson;
