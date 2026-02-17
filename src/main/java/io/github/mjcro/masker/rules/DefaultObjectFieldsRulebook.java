package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.PhoneNumberMasker;
import io.github.mjcro.masker.strings.StringCardPanMasker;
import io.github.mjcro.masker.strings.StringEmailMasker;
import io.github.mjcro.masker.strings.StringFullMasker;
import io.github.mjcro.masker.strings.StringIbanMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringPatternPredicateMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Default rulebook setting to mask structured objects like JSON or XML.
 */
public class DefaultObjectFieldsRulebook implements Rulebook {
    @Override
    public @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameEqualsMaskers() {
        return List.of(
                Rulebook.tuple(
                        StringFullMasker.DEFAULT,
                        "cvv", "cvc", "pin"
                ),
                Rulebook.tuple(
                        StringCardPanMasker.DEFAULT,
                        "card", "cardNumber", "card_number", "pan"
                ),
                Rulebook.tuple(
                        StringIbanMasker.DEFAULT,
                        "iban", "bank_account", "bankAccount"
                ),
                Rulebook.tuple(
                        StringSmartLengthMasker.DEFAULT,
                        "firstName", "first_name", "lastName", "last_name", "beneficiaryName",
                        "ssn", "socialSecurityNumber", "social_security_number",
                        "governmentIdNumber", "government_id_number"
                )
        );
    }

    @Override
    public @NonNull List<Map.Entry<String[], Masker<String, String>>> getNameContainsMaskers() {
        return List.of(
                Rulebook.tuple(
                        StringEmailMasker.DEFAULT,
                        "email"
                ),
                Rulebook.tuple(
                        PhoneNumberMasker.DEFAULT,
                        "phone"
                ),
                Rulebook.tuple(
                        StringSmartLengthMasker.DEFAULT,
                        "login", "password", "street",
                        "key", "token", "consent",
                        "signature", "secret"
                )
        );
    }

    @Override
    public @NonNull List<Masker<String, String>> getInlineMaskers() {
        return List.of(
                StringPatternPredicateMasker.compile(
                        "^[0-9]{12,19}$",
                        StringCardPanMasker.DEFAULT
                ),
                new StringLongTruncationMasker(64)
        );
    }
}
