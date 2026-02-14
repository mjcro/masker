package io.github.mjcro.masker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SequentialMaskerTest {
    @Test
    void testSequentialInvocation() throws Exception {
        SequentialMasker<String> m = new SequentialMasker<>(
                false,
                false,
                Masker.from($ -> $ + $),
                Masker.from($ -> "Hello, " + $)
        );

        Assertions.assertEquals("Hello, FooFoo", m.applyMasking("Foo"));
    }

    @Test
    void testStopOnReferenceCheck() throws Exception {
        SequentialMasker<String> m = new SequentialMasker<>(
                true,
                false,
                Masker.from($ -> $ + $),
                Masker.from($ -> "Hello, " + $)
        );

        Assertions.assertEquals("FooFoo", m.applyMasking("Foo"));
    }

    @Test
    void testStopOnEqualityCheck() throws Exception {
        SequentialMasker<String> m = new SequentialMasker<>(
                false,
                true,
                Masker.from($ -> $ + $),
                Masker.from($ -> "Hello, " + $)
        );

        Assertions.assertEquals("FooFoo", m.applyMasking("Foo"));
    }
}