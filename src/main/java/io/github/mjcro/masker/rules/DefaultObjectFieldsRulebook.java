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

import java.util.List;
import java.util.Map;

/**
 * Opinionated rulebook targeting structured payloads (JSON, XML) with common PII field names:
 * CVV/CVC/PIN, card PAN, IBAN, person name variants, government IDs, email, phone, credentials.
 * Also includes inline maskers for 12–19 digit card-like tokens and a 64-char long-value truncation.
 * Use this as a starting point and subclass or compose further when project-specific rules are required.
 */
public class DefaultObjectFieldsRulebook extends SimpleRulebook {
    public static final List<Map.Entry<String[], Masker<String, String>>> NAME_EQ_MASKERS = List.of(
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
                    "payer.name", "payee.name", "sender.name", "recipient.name", "beneficiary.name",
                    "ssn", "socialSecurityNumber", "social_security_number",
                    "governmentIdNumber", "government_id_number"
            )
    );

    public static final List<Map.Entry<String[], Masker<String, String>>> NAME_CONTAINS_MASKERS = List.of(
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

    public static final List<Masker<String, String>> INLINE_MASKERS = List.of(
            StringPatternPredicateMasker.compile(
                    "^[0-9]{12,19}$",
                    StringCardPanMasker.DEFAULT
            ),
            new StringLongTruncationMasker(64)
    );

    /**
     * Constructs rulebook using UTF-8 charset and no default masker.
     */
    public DefaultObjectFieldsRulebook() {
        super(null, null, INLINE_MASKERS, NAME_EQ_MASKERS, NAME_CONTAINS_MASKERS);
    }
}
