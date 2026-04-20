/**
 * Root package of the MicroJ Masker library for redacting sensitive data
 * (PII, card PANs, IBANs, tokens, HTTP headers, JSON/XML fields).
 *
 * <h2>Core abstraction</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.Masker} — single-method functional interface
 *       {@code R applyMasking(T)}. All other types are concrete maskers, decorators
 *       or assemblers composing maskers from a
 *       {@link io.github.mjcro.masker.rules.Rulebook}.</li>
 * </ul>
 *
 * <h2>Composition primitives in this package</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.SequentialMasker} — chains maskers with
 *       optional short-circuit on first value change.</li>
 *   <li>{@link io.github.mjcro.masker.NameMatchingMaskerDecorator} — applies a
 *       masker only when a field/header name matches a case-insensitive predicate.</li>
 *   <li>{@link io.github.mjcro.masker.SneakyThrowsMaskerDecorator} — rewraps
 *       checked {@link java.lang.Exception} as {@link java.lang.RuntimeException}
 *       for use in lambdas and streams.</li>
 * </ul>
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.strings} — leaf maskers operating on
 *       {@link java.lang.String} values.</li>
 *   <li>{@link io.github.mjcro.masker.rules} — {@code Rulebook} contract and
 *       shipped defaults.</li>
 *   <li>{@link io.github.mjcro.masker.jackson} — JSON document masker built on
 *       Jackson {@code JsonNode}.</li>
 *   <li>{@link io.github.mjcro.masker.xml} — streaming StAX XML masker.</li>
 *   <li>{@link io.github.mjcro.masker.headers} — HTTP header map masker.</li>
 *   <li>{@link io.github.mjcro.masker.util} — case-insensitive name predicates
 *       used by decorators.</li>
 * </ul>
 *
 * <h2>Key conventions</h2>
 * <ul>
 *   <li>Nullability is expressed with JSpecify annotations
 *       ({@code @NonNull}/{@code @Nullable}). Maskers are null-tolerant and
 *       propagate {@code null}/blank inputs unchanged.</li>
 *   <li>Leaf maskers must return the <em>same</em> instance when they perform no
 *       change, because {@code SequentialMasker},
 *       {@link io.github.mjcro.masker.jackson.JsonNodeDocumentMasker} and
 *       {@link io.github.mjcro.masker.xml.XmlStringStaxMasker} detect
 *       modification via reference equality.</li>
 *   <li>{@code Masker#applyMasking} declares {@code throws Exception}; wrap with
 *       {@code SneakyThrowsMaskerDecorator} when checked exceptions are
 *       inconvenient.</li>
 *   <li>Most leaf maskers expose a {@code DEFAULT} singleton preconfigured with
 *       sensible fallbacks.</li>
 * </ul>
 *
 * <h2>Typical assembly</h2>
 * <ol>
 *   <li>Pick or build a {@link io.github.mjcro.masker.rules.Rulebook}.</li>
 *   <li>Obtain a document-level masker via {@code usingRulebook(...)} on
 *       {@code JsonNodeDocumentMasker}, {@code XmlStringStaxMasker} or
 *       {@code HeadersPerNameMasker}.</li>
 *   <li>Call {@code applyMasking(...)} on each payload.</li>
 * </ol>
 */
package io.github.mjcro.masker;
