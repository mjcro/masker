package io.github.mjcro.masker.xml;

import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLOutputFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlStringStaxMaskerTest {
    @Test
    void testMasking() throws Exception {
        String given = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-given.xml").toURI()));
        String expected = Files.readString(Path.of(getClass().getClassLoader().getResource("xml-expected.xml").toURI()));

        System.out.println("Factory Class: " + XMLOutputFactory.newInstance().getClass().getName());
        Assertions.assertEquals(
                expected,
                XmlStringStaxMasker.usingRulebook(new DefaultObjectFieldsRulebook()).applyMasking(given)
        );
    }
}