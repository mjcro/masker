package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.PhoneNumberMasker;
import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringCardPanMasker;
import io.github.mjcro.masker.strings.StringEmailMasker;
import io.github.mjcro.masker.strings.StringFullMasker;
import io.github.mjcro.masker.strings.StringIbanMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RulebookBuilderTest {
    @Test
    void emptyBuilderProducesSafeDefaults() {
        SimpleRulebook rb = Rulebook.builder().build();

        Assertions.assertEquals(StandardCharsets.UTF_8, rb.getCharset());
        Assertions.assertNull(rb.getDefaultMasker());
        Assertions.assertTrue(rb.getInlineMaskers().isEmpty());
        Assertions.assertTrue(rb.getNameEqualsMaskers().isEmpty());
        Assertions.assertTrue(rb.getNameContainsMaskers().isEmpty());
    }

    @Test
    void fluentChainingReturnsSameBuilderInstance() {
        RulebookBuilder b = Rulebook.builder();
        Assertions.assertSame(b, b.withCharset(StandardCharsets.US_ASCII));
        Assertions.assertSame(b, b.withDefaultMasker(StringFullMasker.DEFAULT));
        Assertions.assertSame(b, b.withInlineMasker(StringFullMasker.DEFAULT));
        Assertions.assertSame(b, b.withNameEqualsMasker(StringFullMasker.DEFAULT, "x"));
        Assertions.assertSame(b, b.withNameContainsMasker(StringFullMasker.DEFAULT, "x"));
    }

    @Test
    void withCharsetOverridesDefault() {
        SimpleRulebook rb = Rulebook.builder().withCharset(StandardCharsets.US_ASCII).build();
        Assertions.assertEquals(StandardCharsets.US_ASCII, rb.getCharset());
    }

    @Test
    void withDefaultMaskerSetsFallback() {
        SimpleRulebook rb = Rulebook.builder().withDefaultMasker(StringFullMasker.DEFAULT).build();
        Assertions.assertSame(StringFullMasker.DEFAULT, rb.getDefaultMasker());
    }

    @Test
    void withInlineMaskerAppends() {
        SimpleRulebook rb = Rulebook.builder()
                .withInlineMasker(StringFullMasker.DEFAULT)
                .withInlineMasker(StringEmailMasker.DEFAULT)
                .build();
        List<Masker<String, String>> inline = rb.getInlineMaskers();
        Assertions.assertEquals(2, inline.size());
        Assertions.assertSame(StringFullMasker.DEFAULT, inline.get(0));
        Assertions.assertSame(StringEmailMasker.DEFAULT, inline.get(1));
    }

    @Test
    void withNameEqualsMaskerStoresTuple() {
        SimpleRulebook rb = Rulebook.builder()
                .withNameEqualsMasker(StringIbanMasker.DEFAULT, "iban", "bank_account")
                .build();
        List<Map.Entry<String[], Masker<String, String>>> eq = rb.getNameEqualsMaskers();
        Assertions.assertEquals(1, eq.size());
        Assertions.assertArrayEquals(new String[]{"iban", "bank_account"}, eq.get(0).getKey());
        Assertions.assertSame(StringIbanMasker.DEFAULT, eq.get(0).getValue());
    }

    @Test
    void withNameContainsMaskerStoresTuple() {
        SimpleRulebook rb = Rulebook.builder()
                .withNameContainsMasker(StringEmailMasker.DEFAULT, "email")
                .build();
        List<Map.Entry<String[], Masker<String, String>>> contains = rb.getNameContainsMaskers();
        Assertions.assertEquals(1, contains.size());
        Assertions.assertArrayEquals(new String[]{"email"}, contains.get(0).getKey());
        Assertions.assertSame(StringEmailMasker.DEFAULT, contains.get(0).getValue());
    }

    @Test
    void buildSnapshotIsIsolatedFromFurtherMutation() {
        RulebookBuilder b = Rulebook.builder().withInlineMasker(StringFullMasker.DEFAULT);
        SimpleRulebook snapshot = b.build();
        b.withInlineMasker(StringEmailMasker.DEFAULT);

        Assertions.assertEquals(1, snapshot.getInlineMaskers().size());
        Assertions.assertSame(StringFullMasker.DEFAULT, snapshot.getInlineMaskers().get(0));
    }

    @Test
    void withMaskedCardDataRegistersPanCvvCardholderAndInlinePattern() {
        SimpleRulebook rb = Rulebook.builder().withMaskedCardData().build();

        Assertions.assertSame(StringCardPanMasker.DEFAULT, lookupEquals(rb, "pan"));
        Assertions.assertSame(StringCardPanMasker.DEFAULT, lookupEquals(rb, "card"));
        Assertions.assertSame(StringFullMasker.DEFAULT, lookupEquals(rb, "cvv"));
        Assertions.assertSame(StringFullMasker.DEFAULT, lookupEquals(rb, "cavv"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "cardholderName"));

        Assertions.assertEquals(1, rb.getInlineMaskers().size(),
                "inline PAN pattern masker should be installed");
    }

    @Test
    void withMaskedIdentityRegistersNamesGovIdsAddressesAndStreetContains() {
        SimpleRulebook rb = Rulebook.builder().withMaskedIdentity().build();

        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "firstName"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "passportNumber"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "addressLine1"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupContains(rb, "street"));
    }

    @Test
    void withMaskedContactsRegistersEmailAndPhoneContains() {
        SimpleRulebook rb = Rulebook.builder().withMaskedContacts().build();

        Assertions.assertSame(StringEmailMasker.DEFAULT, lookupContains(rb, "email"));
        Assertions.assertSame(PhoneNumberMasker.DEFAULT, lookupContains(rb, "phone"));
    }

    @Test
    void withMaskedCredentialsRegistersTokenAndPasswordContains() {
        SimpleRulebook rb = Rulebook.builder().withMaskedCredentials().build();

        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupContains(rb, "token"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupContains(rb, "password"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupContains(rb, "secret"));
        Assertions.assertTrue(rb.getNameEqualsMaskers().isEmpty(),
                "JSON credentials bundle must not add name-equals rules");
    }

    @Test
    void withMaskedHeaderCredentialsRegistersAuthorizationRefererAndSessionHeaders() {
        SimpleRulebook rb = Rulebook.builder().withMaskedHeaderCredentials().build();

        Assertions.assertSame(StringAuthorizationHeaderMasker.DEFAULT, lookupEquals(rb, "authorization"));
        Assertions.assertSame(StringUrlRequestUriMasker.DEFAULT, lookupEquals(rb, "referer"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "cookie"));
        Assertions.assertSame(StringSmartLengthMasker.DEFAULT, lookupEquals(rb, "x-api-key"));
        Assertions.assertTrue(rb.getNameContainsMaskers().isEmpty(),
                "header credentials bundle must not add name-contains rules");
    }

    @Test
    void withMaskedIbanRegistersIbanVariants() {
        SimpleRulebook rb = Rulebook.builder().withMaskedIban().build();

        Assertions.assertSame(StringIbanMasker.DEFAULT, lookupEquals(rb, "iban"));
        Assertions.assertSame(StringIbanMasker.DEFAULT, lookupEquals(rb, "bank_account"));
        Assertions.assertSame(StringIbanMasker.DEFAULT, lookupEquals(rb, "bankAccount"));
    }

    @Test
    void withLongValueTruncationNoArgUses64CharThreshold() throws Exception {
        SimpleRulebook rb = Rulebook.builder().withLongValueTruncation().build();

        Assertions.assertEquals(1, rb.getInlineMaskers().size());
        Masker<String, String> masker = rb.getInlineMaskers().get(0);
        Assertions.assertTrue(masker instanceof StringLongTruncationMasker);

        String under = "a".repeat(64);
        Assertions.assertEquals(under, masker.applyMasking(under));

        String over = "a".repeat(70);
        String masked = masker.applyMasking(over);
        Assertions.assertNotEquals(over, masked);
        Assertions.assertTrue(masked.contains("[..."));
    }

    @Test
    void withLongValueTruncationCustomLengthUsesGivenThreshold() throws Exception {
        SimpleRulebook rb = Rulebook.builder().withLongValueTruncation(40).build();
        Masker<String, String> masker = rb.getInlineMaskers().get(0);

        Assertions.assertEquals("a".repeat(40), masker.applyMasking("a".repeat(40)));
        String masked = masker.applyMasking("a".repeat(60));
        Assertions.assertTrue(masked.contains("[..."), "expected truncation marker, got: " + masked);
    }

    @Test
    void bundlesAreAdditiveAndComposable() {
        SimpleRulebook rb = Rulebook.builder()
                .withMaskedIban()
                .withMaskedContacts()
                .withNameEqualsMasker(StringFullMasker.DEFAULT, "apiSecret")
                .build();

        Assertions.assertSame(StringIbanMasker.DEFAULT, lookupEquals(rb, "iban"));
        Assertions.assertSame(StringEmailMasker.DEFAULT, lookupContains(rb, "email"));
        Assertions.assertSame(StringFullMasker.DEFAULT, lookupEquals(rb, "apiSecret"));
    }

    private static Masker<String, String> lookupEquals(SimpleRulebook rb, String name) {
        for (Map.Entry<String[], Masker<String, String>> tuple : rb.getNameEqualsMaskers()) {
            for (String n : tuple.getKey()) {
                if (n.equals(name)) {
                    return tuple.getValue();
                }
            }
        }
        Assertions.fail("name-equals rule not found for: " + name);
        return null;
    }

    private static Masker<String, String> lookupContains(SimpleRulebook rb, String name) {
        for (Map.Entry<String[], Masker<String, String>> tuple : rb.getNameContainsMaskers()) {
            for (String n : tuple.getKey()) {
                if (n.equals(name)) {
                    return tuple.getValue();
                }
            }
        }
        Assertions.fail("name-contains rule not found for: " + name);
        return null;
    }
}
