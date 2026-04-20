package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.NameMatchingMaskerDecorator;
import io.github.mjcro.masker.rules.Rulebook;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Streaming XML masker built on top of StAX event API.
 * Reads the input string as a sequence of XML events, applies field-name maskers to both
 * element character data and attribute values, and re-serializes the stream.
 * Attribute names are tried twice: first as {@code attrName}, then as {@code elementName_attrName}.
 * Comments and processing instructions are passed through unchanged.
 * <p>
 * Uses a hardened {@link XMLInputFactory} with DTD and external entity support disabled.
 * Not thread-safe when different inputs are masked in parallel.
 */
public class XmlStringStaxMasker implements Masker<String, String> {
    private static final XMLInputFactory INPUT_FACTORY = createHardenedInputFactory();
    private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    private static final XMLEventFactory EVENT_FACTORY = XMLEventFactory.newInstance();

    private final Charset charset;
    private final List<Masker<Map.Entry<String, String>, String>> fieldMaskers;
    private final List<Masker<String, String>> inlineMasker;

    private static XMLInputFactory createHardenedInputFactory() {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }

    /**
     * Assembles an XML masker from the given rulebook.
     *
     * @param rulebook Non-null rulebook supplying charset, name maskers and inline maskers.
     * @return Ready-to-use XML masker.
     */
    public static XmlStringStaxMasker usingRulebook(@NonNull Rulebook rulebook) {
        final ArrayList<Masker<Map.Entry<String, String>, String>> fieldMaskers = new ArrayList<>();
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameEqualsMaskers()) {
            fieldMaskers.add(
                    NameMatchingMaskerDecorator.equalsCaseInsensitive(
                            e.getValue(),
                            e.getKey()
                    )
            );
        }
        for (Map.Entry<String[], Masker<String, String>> e : rulebook.getNameContainsMaskers()) {
            fieldMaskers.add(
                    NameMatchingMaskerDecorator.containsCaseInsensitive(
                            e.getValue(),
                            e.getKey()
                    )
            );
        }
        return new XmlStringStaxMasker(
                rulebook.getCharset(),
                fieldMaskers,
                rulebook.getInlineMaskers()
        );
    }

    /**
     * Constructs new XML masker.
     *
     * @param charset       Non-null charset used to decode/encode the XML payload.
     * @param fieldMaskers  Non-null maskers triggered by element or attribute name.
     * @param inlineMaskers Non-null maskers tried on element character data when no field masker matched.
     */
    public XmlStringStaxMasker(
            @NonNull Charset charset,
            @NonNull List<Masker<Map.Entry<String, String>, String>> fieldMaskers,
            @NonNull List<Masker<String, String>> inlineMaskers
    ) {
        this.charset = Objects.requireNonNull(charset, "charset");
        this.fieldMaskers = Objects.requireNonNull(fieldMaskers, "fieldMaskers");
        this.inlineMasker = Objects.requireNonNull(inlineMaskers, "inlineMaskers");
    }

    @Override
    public @Nullable String applyMasking(@Nullable String value) throws Exception {
        if (value == null || value.isBlank()) {
            return value;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final XMLEventReader reader = INPUT_FACTORY.createXMLEventReader(
                    new ByteArrayInputStream(value.getBytes(charset))
            );
            try {
                final XMLEventWriter writer = OUTPUT_FACTORY.createXMLEventWriter(baos);
                try {
                    final ArrayList<String> elementStack = new ArrayList<>();

                    while (reader.hasNext()) {
                        XMLEvent event = reader.nextEvent();

                        switch (event.getEventType()) {
                            case XMLStreamConstants.START_ELEMENT:
                                final StartElement startElement = event.asStartElement();
                                final String currentElement = startElement.getName().getLocalPart();
                                elementStack.add(currentElement);

                                // Handling attributes
                                boolean attributeChanged = false;
                                final Iterator<Attribute> attributes = startElement.getAttributes();
                                final ArrayList<Attribute> newAttributes = new ArrayList<>();
                                while (attributes.hasNext()) {
                                    final Attribute attr = attributes.next();
                                    final String attrLocalPartName = attr.getName().getLocalPart();
                                    final String attrValue = attr.getValue();

                                    // Applying maskers on attr name
                                    String replacement = getReplacement(attrLocalPartName, null, attrValue);
                                    if (!Objects.equals(attrValue, replacement)) {
                                        attributeChanged = true;
                                        newAttributes.add(EVENT_FACTORY.createAttribute(attrLocalPartName, replacement));
                                    } else {
                                        replacement = getReplacement(currentElement + "_" + attrLocalPartName, null, attrValue);
                                        if (!Objects.equals(attrValue, replacement)) {
                                            attributeChanged = true;
                                            newAttributes.add(EVENT_FACTORY.createAttribute(attrLocalPartName, replacement));
                                        } else {
                                            newAttributes.add(attr);
                                        }
                                    }
                                }
                                if (attributeChanged) {
                                    event = EVENT_FACTORY.createStartElement(
                                            startElement.getName(),
                                            newAttributes.iterator(),
                                            startElement.getNamespaces()
                                    );
                                }

                                writer.add(event);
                                break;
                            case XMLStreamConstants.CHARACTERS:
                                final String content = event.asCharacters().getData();
                                final int depth = elementStack.size();
                                final String leafKey = depth > 0 ? elementStack.get(depth - 1) : null;
                                if (content == null || content.isBlank() || leafKey == null) {
                                    writer.add(event);
                                } else {
                                    final String parent = depth > 1 ? elementStack.get(depth - 2) : null;
                                    final String compoundKey = parent == null ? null : parent + "." + leafKey;
                                    final String replacement = getReplacement(leafKey, compoundKey, content);
                                    if (Objects.equals(content, replacement)) {
                                        writer.add(event);
                                    } else {
                                        writer.add(EVENT_FACTORY.createCharacters(replacement));
                                    }
                                }
                                break;
                            case XMLStreamConstants.END_ELEMENT:
                                if (!elementStack.isEmpty()) {
                                    elementStack.remove(elementStack.size() - 1);
                                }
                                writer.add(event);
                                break;
                            default:
                                // Pass through comments, processing instructions, etc.
                                writer.add(event);
                                break;
                        }
                    }
                    writer.flush();
                } finally {
                    writer.close();
                }
            } finally {
                reader.close();
            }

            return baos.toString(charset);
        }
    }

    private String getReplacement(String key, @Nullable String compoundKey, String value) throws Exception {
        String replacement;
        final Map.Entry<String, String> entry = Map.entry(key, value);
        final Map.Entry<String, String> compoundEntry = compoundKey == null ? null : Map.entry(compoundKey, value);
        for (Masker<Map.Entry<String, String>, String> m : fieldMaskers) {
            replacement = m.applyMasking(entry);
            if (!Objects.equals(value, replacement)) {
                return replacement;
            }
            if (compoundEntry != null) {
                replacement = m.applyMasking(compoundEntry);
                if (!Objects.equals(value, replacement)) {
                    return replacement;
                }
            }
        }

        for (Masker<String, String> m : inlineMasker) {
            replacement = m.applyMasking(value);
            if (!Objects.equals(value, replacement)) {
                return replacement;
            }
        }
        return value;
    }
}
