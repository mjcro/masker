package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.NameMatchingMaskerDecorator;
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.strings.StringIbanMasker;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class XmlStringStaxMasker implements Masker<String, String> {
    private final Charset charset;
    private final List<Masker<Map.Entry<String, String>, String>> fieldMaskers;
    private final List<Masker<String, String>> inlineMasker;

    public static XmlStringStaxMasker usingRulebook(@NonNull Rulebook rulebook) {
        ArrayList<Masker<Map.Entry<String, String>, String>> fieldMaskers = new ArrayList<>();
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
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLEventReader reader = inputFactory.createXMLEventReader(
                    new ByteArrayInputStream(value.getBytes(charset))
            );
            XMLEventWriter writer = outputFactory.createXMLEventWriter(baos);
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();

            Deque<String> elementStack = new ArrayDeque<>();

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String currentElement = startElement.getName().getLocalPart();
                        elementStack.push(currentElement);

                        // Handling attributes
                        boolean attributeChanged = false;
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        ArrayList<Attribute> newAttributes = new ArrayList<>();
                        while (attributes.hasNext()) {
                            Attribute attr = attributes.next();
                            String attrLocalPartName = attr.getName().getLocalPart();
                            String attrValue = attr.getValue();

                            // Applying maskers on attr name
                            String replacement = getReplacement(attrLocalPartName, null, attrValue);
                            if (!Objects.equals(attrValue, replacement)) {
                                attributeChanged = true;
                                newAttributes.add(eventFactory.createAttribute(attrLocalPartName, replacement));
                            } else {
                                replacement = getReplacement(currentElement + "_" + attrLocalPartName, null, attrValue);
                                if (!Objects.equals(attrValue, replacement)) {
                                    attributeChanged = true;
                                    newAttributes.add(eventFactory.createAttribute(attrLocalPartName, replacement));
                                } else {
                                    newAttributes.add(attr);
                                }
                            }
                        }
                        if (attributeChanged) {
                            event = eventFactory.createStartElement(
                                    startElement.getName(),
                                    newAttributes.iterator(),
                                    startElement.getNamespaces()
                            );
                        }

                        writer.add(event);
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String content = event.asCharacters().getData();
                        String leafKey = elementStack.peek();
                        if (content == null || content.isBlank() || leafKey == null) {
                            writer.add(event);
                        } else {
                            String compoundKey = null;
                            Iterator<String> it = elementStack.iterator();
                            it.next();
                            if (it.hasNext()) {
                                compoundKey = it.next() + "." + leafKey;
                            }
                            String replacement = getReplacement(leafKey, compoundKey, content);
                            if (Objects.equals(content, replacement)) {
                                writer.add(event);
                            } else {
                                writer.add(eventFactory.createCharacters(replacement));
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        elementStack.pop();
                        writer.add(event);
                        break;
                    default:
                        // Pass through comments, processing instructions, etc.
                        writer.add(event);
                        break;
                }
            }
            writer.flush();
            writer.close();

            baos.flush();

            return baos.toString(charset);
        }
    }

    private String getReplacement(String key, @Nullable String compoundKey, String value) throws Exception {
        // Applying field maskers
        String replacement;
        Map.Entry<String, String> entry = Map.entry(key, value);
        Map.Entry<String, String> compoundEntry = compoundKey == null ? null : Map.entry(compoundKey, value);
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

        // Applying string maskers
        for (Masker<String, String> m : inlineMasker) {
            replacement = m.applyMasking(value);
            if (!Objects.equals(value, replacement)) {
                return replacement;
            }
        }
        return value;
    }

    public static void main(String[] args) throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                "    <metadata>\n" +
                "        <request_id>77b9-441c-8291</request_id>\n" +
                "        <timestamp>2026-02-15T12:00:00Z</timestamp>\n" +
                "    </metadata>\n" +
                "    \n" +
                "    <operations>\n" +
                "        <operation type=\"bank_transfer\" status=\"PENDING\" id=\"OP-1001\">\n" +
                "            <sender name=\"Jane Doe\" ssn=\"123-45-678\">\n" +
                "                <iban>DE21500105170123456789</iban>\n" +
                "            </sender>\n" +
                "            <receiver name=\"John Smith\">\n" +
                "                <iban>GB29NWBK60161331926819</iban>\n" +
                "            </receiver>\n" +
                "            <amount currency=\"EUR\">1250.00</amount>\n" +
                "        </operation>\n" +
                "\n" +
                "        <operation type=\"card_payment\" status=\"SUCCESS\" id=\"OP-1002\">\n" +
                "            <card_details>\n" +
                "                <holder_name>Jane Doe</holder_name>\n" +
                "                <card_number>4111222233334444</card_number>\n" +
                "                <expiry>12/28</expiry>\n" +
                "                <cvv>123</cvv>\n" +
                "            </card_details>\n" +
                "            <amount currency=\"USD\">45.99</amount>\n" +
                "        </operation>\n" +
                "    </operations>\n" +
                "</root>";

        System.out.println(new XmlStringStaxMasker(
                StandardCharsets.UTF_8,
                List.of(
                        NameMatchingMaskerDecorator.equalsCaseInsensitive(
                                StringIbanMasker.DEFAULT,
                                "iban"
                        )
                ),
                List.of()
        ).applyMasking(xml));
    }
}
