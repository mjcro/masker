package io.github.mjcro.masker.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.StringCardPanMasker;
import io.github.mjcro.masker.strings.StringEmailMasker;
import io.github.mjcro.masker.strings.StringFullMasker;
import io.github.mjcro.masker.strings.StringIbanMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringPatternPredicateMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.util.ContainsCaseInsensitivePredicate;
import io.github.mjcro.masker.util.EqualsCaseInsensitivePredicate;

import java.util.List;
import java.util.Map;

public class DefaultJsonDocumentMasker extends JsonDocumentMasker {
    public DefaultJsonDocumentMasker() {
        super(getDefaultFieldMaskers(), getStringMaskers());
    }

    public static List<Masker<Map.Entry<String, JsonNode>, JsonNode>> getDefaultFieldMaskers() {
        return List.of(
                new JsonNodeTextObjectFieldMasker(
                        new EqualsCaseInsensitivePredicate(
                                "firstName", "first_name", "lastName", "last_name", "beneficiaryName",
                                "ssn", "socialSecurityNumber", "social_security_number",
                                "governmentIdNumber", "government_id_number"
                        ).or(new ContainsCaseInsensitivePredicate(
                                "login", "password", "phone", "street",
                                "key", "token", "consent",
                                "signature"
                        )),
                        StringSmartLengthMasker.DEFAULT
                ),
                new JsonNodeTextObjectFieldMasker(
                        new EqualsCaseInsensitivePredicate(
                                "card", "cardNumber", "card_number", "pan"
                        ),
                        StringCardPanMasker.DEFAULT
                ),
                new JsonNodeTextObjectFieldMasker(
                        new EqualsCaseInsensitivePredicate(
                                "iban", "bank_account", "bankAccount"
                        ),
                        StringIbanMasker.DEFAULT
                ),
                new JsonNodeTextObjectFieldMasker(
                        new ContainsCaseInsensitivePredicate("email"),
                        StringEmailMasker.DEFAULT
                ),
                new JsonNodeTextObjectFieldMasker(
                        new EqualsCaseInsensitivePredicate(
                                "cvv", "cvc", "pin"
                        ),
                        StringFullMasker.DEFAULT
                )
        );
    }

    public static List<Masker<CharSequence, String>> getStringMaskers() {
        return List.of(
                StringPatternPredicateMasker.compile(
                        "^[0-9]{12,19}$",
                        StringCardPanMasker.DEFAULT
                ),
                new StringLongTruncationMasker(64)
        );
    }
}
