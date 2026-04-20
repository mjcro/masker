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
 *
 * @deprecated Prefer composing the equivalent configuration with {@link Rulebook#builder()},
 * which lets callers pick the subset of rules they actually need. The full
 * content of this rulebook is reproduced by:
 * <pre>{@code
 * Rulebook.builder()
 *     .withMaskedCardData()
 *     .withMaskedIdentity()
 *     .withMaskedContacts()
 *     .withMaskedCredentials()
 *     .withMaskedIban()
 *     .withLongValueTruncation()
 *     .build();
 * }</pre>
 * This class will be removed in a future release.
 */
@Deprecated
public class DefaultObjectFieldsRulebook extends SimpleRulebook {
    public static final List<Map.Entry<String[], Masker<String, String>>> NAME_EQ_MASKERS = List.of(
            Rulebook.tuple(
                    StringFullMasker.DEFAULT,
                    // Card verification values (generic + brand-specific: Amex CID, JCB CAV2, Discover CSC).
                    "cvv", "cvc", "pin", "cvv2", "cvc2", "cid", "cav2", "csc",
                    // Magnetic-stripe / chip track data — PCI-DSS SAD, storage prohibited.
                    "track1", "track2", "trackData", "track_data",
                    "magstripe", "emvData", "emv_data", "iccData", "icc_data",
                    // 3-D Secure cryptographic authentication value (replay risk if PAN leaks).
                    "cavv", "authenticationValue", "authentication_value",
                    // Network / wallet tokens that act as PAN substitutes for the merchant scope.
                    "dpan",
                    "networkToken", "network_token",
                    "applePayToken", "apple_pay_token",
                    "googlePayToken", "google_pay_token"
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
                    // Cardholder name variants specific to card payloads.
                    "cardholder", "cardholder.name", "cardholderName", "cardholder_name",
                    "nameOnCard", "name_on_card",
                    "ssn", "socialSecurityNumber", "social_security_number",
                    "governmentIdNumber", "government_id_number",
                    // KYC document numbers (passport, national ID, driver's licence, generic).
                    "passportNumber", "passport_number",
                    "idCardNumber", "id_card_number",
                    "driverLicense", "driver_license", "driverLicence", "driver_licence",
                    "documentNumber", "document_number",
                    "nationalId", "national_id",
                    // US tax identifiers (TIN frequently is an SSN; EIN/ITIN are same sensitivity class).
                    "tin", "ein", "itin",
                    // Building-level address components (city/postal code stay visible for AVS/triage).
                    "addressLine1", "address_line_1", "addressLine2", "address_line_2",
                    "houseNumber", "house_number", "apartment"
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
