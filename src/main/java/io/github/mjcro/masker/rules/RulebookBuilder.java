package io.github.mjcro.masker.rules;

import io.github.mjcro.masker.Masker;
import io.github.mjcro.masker.strings.PhoneNumberMasker;
import io.github.mjcro.masker.strings.StringAuthorizationHeaderMasker;
import io.github.mjcro.masker.strings.StringCardPanMasker;
import io.github.mjcro.masker.strings.StringEmailMasker;
import io.github.mjcro.masker.strings.StringFullMasker;
import io.github.mjcro.masker.strings.StringIbanMasker;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;
import io.github.mjcro.masker.strings.StringPatternPredicateMasker;
import io.github.mjcro.masker.strings.StringSmartLengthMasker;
import io.github.mjcro.masker.strings.StringUrlRequestUriMasker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent builder for {@link SimpleRulebook} instances.
 *
 * <p>Exposes two layers of composition:
 * <ul>
 *   <li><strong>Low-level withers</strong> — {@link #withNameEqualsMasker},
 *       {@link #withNameContainsMasker}, {@link #withInlineMasker},
 *       {@link #withDefaultMasker}, {@link #withCharset} — for precise, one-off
 *       rule additions.</li>
 *   <li><strong>Bundle withers</strong> — {@code withMasked*()} methods that
 *       install an opinionated cluster of rules for a given data family
 *       (card data, personal identity, contacts, credentials, banking, etc.).
 *       Calling several bundles assembles a full-fledged rulebook with a
 *       single fluent chain.</li>
 * </ul>
 *
 * <p>All bundle withers are additive: they append tuples and never clear or
 * replace prior configuration, so bundles can be combined freely with each
 * other and with the low-level withers.
 *
 * <p>Instances are mutable and not thread-safe. {@link #build()} produces an
 * immutable {@link SimpleRulebook} that snapshots the current configuration.
 */
public final class RulebookBuilder {
    private @NonNull Charset charset = StandardCharsets.UTF_8;
    private @Nullable Masker<String, String> defaultMasker;
    private final List<Masker<String, String>> inlineMaskers = new ArrayList<>();
    private final List<Map.Entry<String[], Masker<String, String>>> nameEqualsMaskers = new ArrayList<>();
    private final List<Map.Entry<String[], Masker<String, String>>> nameContainsMaskers = new ArrayList<>();

    /**
     * Sets the charset used by maskers operating on encoded byte streams.
     *
     * @param charset Non-null charset.
     * @return This builder for chaining.
     */
    public RulebookBuilder withCharset(@NonNull Charset charset) {
        this.charset = Objects.requireNonNull(charset, "charset");
        return this;
    }

    /**
     * Sets the fallback masker applied when no other rule matches. Overrides any
     * previously configured default.
     *
     * @param masker Non-null fallback masker.
     * @return This builder for chaining.
     */
    public RulebookBuilder withDefaultMasker(@NonNull Masker<String, String> masker) {
        this.defaultMasker = Objects.requireNonNull(masker, "masker");
        return this;
    }

    /**
     * Appends an inline masker applied unconditionally to every textual leaf.
     *
     * @param masker Non-null masker.
     * @return This builder for chaining.
     */
    public RulebookBuilder withInlineMasker(@NonNull Masker<String, String> masker) {
        this.inlineMaskers.add(Objects.requireNonNull(masker, "masker"));
        return this;
    }

    /**
     * Appends a name-equals rule: {@code masker} is applied to fields or headers
     * whose name matches any of {@code names} (case-insensitive, exact).
     *
     * @param masker Non-null masker.
     * @param names  Non-null, non-empty names.
     * @return This builder for chaining.
     */
    public RulebookBuilder withNameEqualsMasker(
            @NonNull Masker<String, String> masker,
            @NonNull String @NonNull ... names
    ) {
        Objects.requireNonNull(masker, "masker");
        Objects.requireNonNull(names, "names");
        this.nameEqualsMaskers.add(Rulebook.tuple(masker, names));
        return this;
    }

    /**
     * Appends a name-contains rule: {@code masker} is applied to fields or
     * headers whose name contains any of {@code substrings} (case-insensitive).
     *
     * @param masker     Non-null masker.
     * @param substrings Non-null, non-empty substrings.
     * @return This builder for chaining.
     */
    public RulebookBuilder withNameContainsMasker(
            @NonNull Masker<String, String> masker,
            @NonNull String @NonNull ... substrings
    ) {
        Objects.requireNonNull(masker, "masker");
        Objects.requireNonNull(substrings, "substrings");
        this.nameContainsMaskers.add(Rulebook.tuple(masker, substrings));
        return this;
    }

    /**
     * Installs rules for card and payment data: PAN, CVV/PIN family, magstripe
     * and EMV track data, 3-D Secure authentication values, network / wallet
     * tokens, and cardholder name variants. Also installs an inline detector
     * for bare 12–19 digit PAN-like strings.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedCardData() {
        return this
                .withNameEqualsMasker(
                        StringCardPanMasker.DEFAULT,
                        "card", "cardNumber", "card_number", "pan"
                )
                .withNameEqualsMasker(
                        StringFullMasker.DEFAULT,
                        "cvv", "cvc", "pin", "cvv2", "cvc2", "cid", "cav2", "csc",
                        "track1", "track2", "trackData", "track_data",
                        "magstripe", "emvData", "emv_data", "iccData", "icc_data",
                        "cavv", "authenticationValue", "authentication_value",
                        "dpan",
                        "networkToken", "network_token",
                        "applePayToken", "apple_pay_token",
                        "googlePayToken", "google_pay_token"
                )
                .withNameEqualsMasker(
                        StringSmartLengthMasker.DEFAULT,
                        "cardholder", "cardholder.name", "cardholderName", "cardholder_name",
                        "nameOnCard", "name_on_card"
                )
                .withInlineMasker(StringPatternPredicateMasker.compile(
                        "^[0-9]{12,19}$",
                        StringCardPanMasker.DEFAULT
                ));
    }

    /**
     * Installs rules for personal identity data: person name variants (including
     * payer/payee/sender/recipient/beneficiary sub-fields), government IDs
     * (SSN, passport, driver licence, national ID, TIN/EIN/ITIN) and
     * building-level address components. Also masks any field whose name
     * contains {@code street}.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedIdentity() {
        return this
                .withNameEqualsMasker(
                        StringSmartLengthMasker.DEFAULT,
                        "firstName", "first_name", "lastName", "last_name", "beneficiaryName",
                        "payer.name", "payee.name", "sender.name", "recipient.name", "beneficiary.name",
                        "ssn", "socialSecurityNumber", "social_security_number",
                        "governmentIdNumber", "government_id_number",
                        "passportNumber", "passport_number",
                        "idCardNumber", "id_card_number",
                        "driverLicense", "driver_license", "driverLicence", "driver_licence",
                        "documentNumber", "document_number",
                        "nationalId", "national_id",
                        "tin", "ein", "itin",
                        "addressLine1", "address_line_1", "addressLine2", "address_line_2",
                        "houseNumber", "house_number", "apartment"
                )
                .withNameContainsMasker(StringSmartLengthMasker.DEFAULT, "street");
    }

    /**
     * Installs rules for contact data: any field whose name contains
     * {@code email} or {@code phone}.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedContacts() {
        return this
                .withNameContainsMasker(StringEmailMasker.DEFAULT, "email")
                .withNameContainsMasker(PhoneNumberMasker.DEFAULT, "phone");
    }

    /**
     * Installs JSON/XML-oriented rules for credentials: masks any field whose
     * name contains {@code login}, {@code password}, {@code key}, {@code token},
     * {@code consent}, {@code signature} or {@code secret} (case-insensitive).
     * For HTTP header credentials use {@link #withMaskedHeaderCredentials()}.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedCredentials() {
        return this.withNameContainsMasker(
                StringSmartLengthMasker.DEFAULT,
                "login", "password", "key", "token", "consent", "signature", "secret"
        );
    }

    /**
     * Installs HTTP-header-oriented rules for credentials: exact-match masking
     * of {@code Authorization} (with dedicated authorization-header masker),
     * {@code Referer} (with URI masker), {@code Proxy-Authorization},
     * {@code Cookie}/{@code Set-Cookie}, {@code WWW-Authenticate} and common
     * {@code X-*-Token}/{@code X-Api-Key}/consent-token header variants.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedHeaderCredentials() {
        return this
                .withNameEqualsMasker(StringAuthorizationHeaderMasker.DEFAULT, "authorization")
                .withNameEqualsMasker(StringUrlRequestUriMasker.DEFAULT, "referer")
                .withNameEqualsMasker(
                        StringSmartLengthMasker.DEFAULT,
                        "proxy-authorization", "cookie", "set-cookie",
                        "token", "signature", "consent",
                        "x-token", "x-auth-token", "x-api-key", "x-key",
                        "www-authenticate", "x-itc-token", "consent-token", "consenttoken"
                );
    }

    /**
     * Installs rules for banking data: masks {@code iban}, {@code bank_account}
     * and {@code bankAccount} with the IBAN-aware masker.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withMaskedIban() {
        return this.withNameEqualsMasker(
                StringIbanMasker.DEFAULT,
                "iban", "bank_account", "bankAccount"
        );
    }

    /**
     * Installs an inline {@link StringLongTruncationMasker} with the default
     * maximum length of 64 characters.
     *
     * @return This builder for chaining.
     */
    public RulebookBuilder withLongValueTruncation() {
        return this.withLongValueTruncation(64);
    }

    /**
     * Installs an inline {@link StringLongTruncationMasker} with the given
     * maximum length.
     *
     * @param maxLength Maximum retained length in characters; must be positive.
     * @return This builder for chaining.
     */
    public RulebookBuilder withLongValueTruncation(int maxLength) {
        return this.withInlineMasker(new StringLongTruncationMasker(maxLength));
    }

    /**
     * Builds an immutable {@link SimpleRulebook} snapshotting the current
     * configuration. Further mutations of this builder do not affect the
     * returned rulebook.
     *
     * @return New rulebook.
     */
    public SimpleRulebook build() {
        return new SimpleRulebook(
                charset,
                defaultMasker,
                List.copyOf(inlineMaskers),
                List.copyOf(nameEqualsMaskers),
                List.copyOf(nameContainsMaskers)
        );
    }
}
