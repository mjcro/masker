/**
 * Document-level masker for XML payloads built on the StAX event API.
 *
 * <h2>Entry point</h2>
 * <ul>
 *   <li>{@link io.github.mjcro.masker.xml.XmlStringStaxMasker} — streaming
 *       rewriter that accepts an XML string and returns a masked XML string.
 *       Assembled from a {@link io.github.mjcro.masker.rules.Rulebook} via
 *       {@code usingRulebook(...)}.</li>
 * </ul>
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>Element character data is masked using the current element's local name
 *       as the lookup key; for nested elements the compound key
 *       {@code parent.child} is tried as well.</li>
 *   <li>Attribute values are tried twice: first with the attribute's local name,
 *       then with {@code elementName_attributeName}.</li>
 *   <li>Comments and processing instructions pass through unchanged.</li>
 *   <li>The underlying {@link javax.xml.stream.XMLInputFactory} is hardened:
 *       DTD loading and external entity resolution are disabled to mitigate
 *       XXE attacks.</li>
 *   <li>Modification is detected via {@link java.util.Objects#equals}; leaf
 *       maskers returning an equal value do not alter the stream.</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 *
 * <p>Requires any JAXP-compatible StAX implementation on the classpath. The JDK
 * ships one; Woodstox is recommended for high-throughput workloads.
 */
package io.github.mjcro.masker.xml;
