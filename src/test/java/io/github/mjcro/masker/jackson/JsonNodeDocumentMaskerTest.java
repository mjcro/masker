package io.github.mjcro.masker.jackson;

import io.github.mjcro.masker.rules.Rulebook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JsonNodeDocumentMaskerTest {
    private static final Rulebook RULEBOOK = Rulebook.builder()
            .withMaskedCardData()
            .withMaskedIdentity()
            .withMaskedContacts()
            .withMaskedCredentials()
            .withMaskedIban()
            .withLongValueTruncation()
            .build();

    @Test
    void testMasking() throws Exception {
        String given = Files.readString(Path.of(getClass().getClassLoader().getResource("json-given.json").toURI()));
        String expected = Files.readString(Path.of(getClass().getClassLoader().getResource("json-expected.json").toURI()));

        Assertions.assertEquals(
                expected,
                JsonNodeDocumentMasker.usingRulebook(RULEBOOK).maskJsonPrettyString(given)
        );
    }

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // Object keys
                Arguments.of("{\"firstNAME\":\"Alexander\"}", "{\"firstNAME\":\"A***\"}"),
                Arguments.of("{\"FIRST_name\":\"Alexander\"}", "{\"FIRST_name\":\"A***\"}"),
                Arguments.of("{\"laSTname\":\"Richardson\"}", "{\"laSTname\":\"R***n\"}"),
                Arguments.of("{\"last_NAME\":\"Richardson\"}", "{\"last_NAME\":\"R***n\"}"),
                Arguments.of("{\"Login\":\"abcdef\"}", "{\"Login\":\"a***\"}"),
                Arguments.of("{\"merchant_login\":\"abcdef\"}", "{\"merchant_login\":\"a***\"}"),
                Arguments.of("{\"Password\":\"abcdef\"}", "{\"Password\":\"a***\"}"),
                Arguments.of("{\"merchant_password\":\"abcdef\"}", "{\"merchant_password\":\"a***\"}"),
                Arguments.of("{\"cVv\":\"1234\"}", "{\"cVv\":\"***\"}"),
                Arguments.of("{\"CVc\":\"1234\"}", "{\"CVc\":\"***\"}"),
                // CVV brand variants (Amex CID, JCB CAV2, Discover CSC)
                Arguments.of("{\"cvv2\":\"1234\"}", "{\"cvv2\":\"***\"}"),
                Arguments.of("{\"CVC2\":\"1234\"}", "{\"CVC2\":\"***\"}"),
                Arguments.of("{\"cid\":\"1234\"}", "{\"cid\":\"***\"}"),
                Arguments.of("{\"cav2\":\"1234\"}", "{\"cav2\":\"***\"}"),
                Arguments.of("{\"csc\":\"1234\"}", "{\"csc\":\"***\"}"),
                // Magstripe / track data
                Arguments.of("{\"track2\":\";1234567890123456=1234567890?\"}", "{\"track2\":\"***\"}"),
                Arguments.of("{\"trackData\":\"%B4111111111111111^DOE/JOHN^2512?\"}", "{\"trackData\":\"***\"}"),
                Arguments.of("{\"emv_data\":\"9F2608abcd\"}", "{\"emv_data\":\"***\"}"),
                // 3-D Secure authentication value
                Arguments.of("{\"cavv\":\"AAABBwAgAABQBw==\"}", "{\"cavv\":\"***\"}"),
                Arguments.of("{\"authenticationValue\":\"AAABBwAgAABQBw==\"}", "{\"authenticationValue\":\"***\"}"),
                // Network / wallet tokens (PAN-equivalent)
                Arguments.of("{\"networkToken\":\"tok_abcdef1234\"}", "{\"networkToken\":\"***\"}"),
                Arguments.of("{\"network_token\":\"tok_abcdef1234\"}", "{\"network_token\":\"***\"}"),
                Arguments.of("{\"dpan\":\"4111111111111111\"}", "{\"dpan\":\"***\"}"),
                Arguments.of("{\"applePayToken\":\"tok_abcdef1234\"}", "{\"applePayToken\":\"***\"}"),
                Arguments.of("{\"google_pay_token\":\"tok_abcdef1234\"}", "{\"google_pay_token\":\"***\"}"),
                Arguments.of("{\"email\":\"theverylongemail@gmail.com\"}", "{\"email\":\"t***l@gmail.com\"}"),
                Arguments.of("{\"pan\":\"1234561111119876\"}", "{\"pan\":\"***9876\"}"),
                Arguments.of("{\"iban\":\"UA1234561111119876\"}", "{\"iban\":\"UA12***9876\"}"),
                // Cardholder name variants
                Arguments.of("{\"cardholderName\":\"John Smith\"}", "{\"cardholderName\":\"J***h\"}"),
                Arguments.of("{\"nameOnCard\":\"John Smith\"}", "{\"nameOnCard\":\"J***h\"}"),
                Arguments.of("{\"name_on_card\":\"John Smith\"}", "{\"name_on_card\":\"J***h\"}"),
                Arguments.of("{\"cardholder\":{\"name\":\"John Smith\"}}", "{\"cardholder\":{\"name\":\"J***h\"}}"),
                // KYC document numbers
                Arguments.of("{\"passportNumber\":\"X1234567\"}", "{\"passportNumber\":\"X***\"}"),
                Arguments.of("{\"driver_license\":\"DL1234567890\"}", "{\"driver_license\":\"D***0\"}"),
                Arguments.of("{\"nationalId\":\"AB1234567\"}", "{\"nationalId\":\"A***\"}"),
                // US tax IDs
                Arguments.of("{\"tin\":\"123-45-6789\"}", "{\"tin\":\"1***9\"}"),
                Arguments.of("{\"EIN\":\"12-3456789\"}", "{\"EIN\":\"1***9\"}"),
                // Address line components
                Arguments.of("{\"addressLine1\":\"221B Baker Street\"}", "{\"addressLine1\":\"2***t\"}"),
                Arguments.of("{\"houseNumber\":\"221B\"}", "{\"houseNumber\":\"2***\"}"),
                Arguments.of("{\"apartment\":\"4B\"}", "{\"apartment\":\"***\"}"),
                // Compound (parent.leaf) matching
                Arguments.of("{\"payer\":{\"name\":\"Abraham Lincoln\"}}", "{\"payer\":{\"name\":\"A***n\"}}"),
                Arguments.of("{\"payee\":{\"name\":\"Abraham Lincoln\"}}", "{\"payee\":{\"name\":\"A***n\"}}"),
                Arguments.of("{\"sender\":{\"name\":\"Abraham Lincoln\"}}", "{\"sender\":{\"name\":\"A***n\"}}"),
                Arguments.of("{\"recipient\":{\"name\":\"Abraham Lincoln\"}}", "{\"recipient\":{\"name\":\"A***n\"}}"),
                Arguments.of("{\"beneficiary\":{\"name\":\"Abraham Lincoln\"}}", "{\"beneficiary\":{\"name\":\"A***n\"}}"),
                Arguments.of("{\"PAYER\":{\"Name\":\"Abraham Lincoln\"}}", "{\"PAYER\":{\"Name\":\"A***n\"}}"),
                // Bare "name" at root or under an unknown parent is not masked
                Arguments.of("{\"name\":\"Abraham Lincoln\"}", "{\"name\":\"Abraham Lincoln\"}"),
                Arguments.of("{\"other\":{\"name\":\"Abraham Lincoln\"}}", "{\"other\":{\"name\":\"Abraham Lincoln\"}}"),
                // Inline
                Arguments.of("\"1234561111119876\"", "\"***9876\"")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMasking(final String given, final String expected) throws Exception {
        Assertions.assertEquals(
                expected,
                JsonNodeDocumentMasker.usingRulebook(RULEBOOK).maskJsonString(given)
        );
    }
}
