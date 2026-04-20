# MicroJ Masker

Tiny Java 11+ library for masking sensitive data (PII, card PANs, IBANs, tokens, HTTP
headers, JSON/XML fields) in logs, audit trails and API responses.

- **Runtime dependency**: [JSpecify](https://jspecify.dev/) annotations only.
- **Optional**: `jackson-databind` for JSON document masking (declared `provided`),
  a StAX implementation (e.g. Woodstox) for XML document masking.
- **Source/target**: Java 11. CI matrix: 11, 17, 21, 25.
- **License**: MIT.

## Install

Maven Central coordinates:

```xml
<dependency>
    <groupId>io.github.mjcro</groupId>
    <artifactId>masker</artifactId>
    <version>0.0.4</version>
</dependency>
```

If you want JSON masking, also add Jackson on your classpath:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

For XML masking any JAXP-compatible StAX implementation will do (the JDK ships one;
Woodstox is recommended for large payloads).

## Quick start

### Mask a JSON string

```java
import io.github.mjcro.masker.jackson.JsonNodeDocumentMasker;
import io.github.mjcro.masker.rules.Rulebook;

JsonNodeDocumentMasker masker = JsonNodeDocumentMasker.usingRulebook(
    Rulebook.builder()
        .withMaskedCardData()
        .withMaskedIdentity()
        .withMaskedContacts()
        .withMaskedCredentials()
        .withMaskedIban()
        .withLongValueTruncation()
        .build()
);

String masked = masker.maskJsonString(
    "{\"email\":\"alice@example.com\",\"card\":\"4111111111111111\",\"cvv\":\"123\"}"
);
// {"email":"a***@example.com","card":"***1111","cvv":"***"}
```

### Mask an XML string

```java
import io.github.mjcro.masker.xml.XmlStringStaxMasker;
import io.github.mjcro.masker.rules.Rulebook;

XmlStringStaxMasker masker = XmlStringStaxMasker.usingRulebook(
    Rulebook.builder()
        .withMaskedCardData()
        .withMaskedContacts()
        .build()
);
String masked = masker.applyMasking("<user><email>alice@example.com</email></user>");
```

### Mask HTTP headers

```java
import io.github.mjcro.masker.headers.HeadersPerNameMasker;
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.strings.StringLongTruncationMasker;

HeadersPerNameMasker masker = HeadersPerNameMasker.usingRulebook(
    Rulebook.builder()
        .withMaskedHeaderCredentials()
        .withDefaultMasker(new StringLongTruncationMasker(64))
        .build()
);
Map<String, List<String>> masked = masker.applyMasking(request.getHeaders());
```

### Use a single string masker

```java
import io.github.mjcro.masker.strings.StringCardPanMasker;

String masked = StringCardPanMasker.DEFAULT.applyMasking("4111-1111-1111-1111");
// "***1111"
```

## Architecture

Everything is built on one functional interface — `Masker<T, R>` with
`R applyMasking(T value) throws Exception`. Concrete types fall into three roles:

1. **Leaf maskers** — operate on a single string or value.
2. **Decorators / composites** — wrap other maskers (conditional, chained, exception-wrapping).
3. **Document maskers** — walk a JSON tree, XML stream or header map and dispatch
   the right leaf masker per field.

Document maskers are assembled from a `Rulebook` via `usingRulebook(...)`.

### Leaf string maskers (`io.github.mjcro.masker.strings`)

| Class                              | Purpose                                                |
|------------------------------------|--------------------------------------------------------|
| `StringFullMasker`                 | Replace value entirely with `***`.                     |
| `StringSmartLengthMasker`          | Keep short prefix/suffix depending on input length.    |
| `StringCardPanMasker`              | Card PAN — keep last 4 digits.                         |
| `StringIbanMasker`                 | IBAN — keep first 4 (and last 4 for long IBANs).       |
| `StringEmailMasker`                | Mask local part, keep domain.                          |
| `PhoneNumberMasker`                | Keep last 2 digits.                                    |
| `StringAuthorizationHeaderMasker`  | `Bearer abc***` style for auth headers.                |
| `StringUrlRequestUriMasker`        | Keep scheme + host, mask path and query.               |
| `StringLongTruncationMasker`       | Shorten oversized log entries with `[...N...]` marker. |
| `StringPatternPredicateMasker`     | Apply another masker only on full regex match.         |
| `StringInlinePatternMasker`        | Find regex matches inside text and mask each one.      |

Most expose a `DEFAULT` singleton preconfigured with sensible fallbacks.

### Composition

- `SequentialMasker<T>` — chains maskers and optionally short-circuits as soon as one
  of them changes the value (by reference or by `equals`).
- `NameMatchingMaskerDecorator<T>` — applies a masker only when the field/header
  name matches (case-insensitive exact or substring match).
- `SneakyThrowsMaskerDecorator<T,R>` — rewraps checked `Exception` as
  `RuntimeException`, so maskers work inside `Stream` and `Function`.

### Rulebooks (`io.github.mjcro.masker.rules`)

A `Rulebook` bundles four optional categories plus a charset:

- `getNameEqualsMaskers()` — `[name1, name2, ...] -> masker`, exact match.
- `getNameContainsMaskers()` — `[substr1, substr2, ...] -> masker`, substring match.
- `getInlineMaskers()` — unconditional maskers applied to every textual leaf.
- `getDefaultMasker()` — fallback for anything else.

A consumer is allowed to use only the subset it needs, so not every document masker
reacts to every category. Build rulebooks with `Rulebook.builder()`:

- Granular withers — `withNameEqualsMasker`, `withNameContainsMasker`,
  `withInlineMasker`, `withDefaultMasker`, `withCharset`.
- **Bundle withers** install opinionated rule clusters with a single call:
  - `withMaskedCardData()` — PAN, CVV/PIN family, track/EMV data, 3-D Secure
    authentication values, network / wallet tokens, cardholder names, and an
    inline 12–19-digit PAN detector.
  - `withMaskedIdentity()` — person name variants, government IDs (SSN, passport,
    driver licence, TIN/EIN/ITIN), building-level address components + `street` contains.
  - `withMaskedContacts()` — `email` and `phone` (contains).
  - `withMaskedCredentials()` — JSON-side credentials: contains `login` / `password`
    / `key` / `token` / `consent` / `signature` / `secret`.
  - `withMaskedHeaderCredentials()` — HTTP header credentials: exact-match on
    `Authorization`, `Referer`, `Proxy-Authorization`, `Cookie`/`Set-Cookie`,
    `WWW-Authenticate`, `X-*-Token`, `X-Api-Key`, consent-token variants.
  - `withMaskedIban()` — `iban`, `bank_account`, `bankAccount`.
  - `withLongValueTruncation(maxLength)` — appends a
    `StringLongTruncationMasker` as an inline rule (64 chars by default).

Shipped types:

- `RulebookBuilder` — fluent builder returned by `Rulebook.builder()`.
- `SimpleRulebook` — immutable container produced by `build()`.
- `DefaultObjectFieldsRulebook` — **deprecated**; see [Default object-fields coverage](#default-object-fields-coverage) below. Equivalent to
  `Rulebook.builder().withMaskedCardData().withMaskedIdentity().withMaskedContacts().withMaskedCredentials().withMaskedIban().withLongValueTruncation().build()`.
- `DefaultHttpHeadersRulebook` — **deprecated**. Equivalent to
  `Rulebook.builder().withMaskedHeaderCredentials().withDefaultMasker(new StringLongTruncationMasker(64)).build()`.

#### Default object-fields coverage

Calling `withMaskedCardData().withMaskedIdentity().withMaskedContacts().withMaskedCredentials().withMaskedIban().withLongValueTruncation()`
on the builder installs an opinionated configuration aimed at request/response logging: fields
that are prohibited to store (PCI-DSS SAD) or strongly re-identifying are erased entirely,
whereas fields that are still useful for debugging (a last-4, a length-hint) are
partially masked. Anything that is only dangerous *in combination with a masked PAN*
(expiry, BIC, routing number, 3DS correlation IDs) is left visible.

Fully masked (`***`):

- **Card verification values** — `cvv`, `cvc`, `pin`, `cvv2`, `cvc2`, `cid`, `cav2`, `csc`.
- **Magstripe / chip track data** — `track1`, `track2`, `trackData`, `magstripe`, `emvData`, `iccData` (+ `*_data` forms).
- **3-D Secure authentication value** — `cavv`, `authenticationValue`.
- **Network / wallet tokens (PAN-equivalent)** — `dpan`, `networkToken`, `applePayToken`, `googlePayToken` (+ `snake_case` forms).

Partially masked (smart-length — keeps prefix/suffix so log lines stay debuggable):

- **Cardholder name** — `cardholder`, `cardholder.name`, `cardholderName`, `nameOnCard` (+ `snake_case` forms).
- **KYC document numbers** — `passportNumber`, `idCardNumber`, `driverLicense` / `driverLicence`, `documentNumber`, `nationalId` (+ `snake_case` forms).
- **US tax identifiers** — `tin`, `ein`, `itin`.
- **Building-level address components** — `addressLine1`, `addressLine2`, `houseNumber`, `apartment` (+ `snake_case` forms). City / state / postal code are intentionally left visible for AVS and fraud triage.

Previously covered and unchanged: PAN (`card`, `pan`, `cardNumber`), IBAN / bank account,
email, phone, person names under `payer|payee|sender|recipient|beneficiary`, `ssn` /
`governmentIdNumber`, free-form `login` / `password` / `token` / `secret` / `key` /
`signature` / `consent`. An inline 12–19-digit PAN detector and a 64-character
truncation masker also run on every textual leaf.

> **Note on `cid`:** in payment payloads this is an Amex card-verification field and
> is erased. If your logs use `cid` as a generic correlation ID, omit `withMaskedCardData()`
> and register the specific card-verification fields you need with `withNameEqualsMasker(...)`.

### Document-level maskers

| Class                              | Input                               | Notes                                                                                  |
|------------------------------------|-------------------------------------|----------------------------------------------------------------------------------------|
| `JsonNodeDocumentMasker`           | Jackson `JsonNode` or JSON string   | Walks the tree, mutates in place. Handles `parent.child` compound names and primitive arrays. |
| `XmlStringStaxMasker`              | XML string                          | Streaming StAX rewriter with hardened DTD/external-entity settings. Masks element text and attributes. |
| `HeadersPerNameMasker`             | `Map<String, List<String>>`         | Per-name lookup (case-insensitive) with a default-masker fallback.                     |

## Custom rulebook example

```java
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.strings.*;

Rulebook rb = Rulebook.builder()
    .withDefaultMasker(StringLongTruncationMasker.DEFAULT)
    .withNameEqualsMasker(StringFullMasker.DEFAULT, "cvv", "pin")
    .withNameEqualsMasker(StringCardPanMasker.DEFAULT, "card", "pan")
    .withNameContainsMasker(StringEmailMasker.DEFAULT, "email")
    .withNameContainsMasker(PhoneNumberMasker.DEFAULT, "phone")
    .build();
```

Combine granular withers with bundle withers freely — they are additive:

```java
Rulebook rb = Rulebook.builder()
    .withMaskedCardData()
    .withMaskedContacts()
    .withNameEqualsMasker(StringFullMasker.DEFAULT, "internalSecret")
    .build();
```

## Conventions

- **Nullability** uses JSpecify (`@NonNull`/`@Nullable`). Inputs are null-tolerant;
  most maskers pass `null`/blank through unchanged.
- **Checked exceptions**: `Masker.applyMasking(...)` declares `throws Exception`.
  The StAX and Jackson paths genuinely throw checked exceptions — either catch them
  or wrap the masker with `SneakyThrowsMaskerDecorator`.
- **Reference equality** is used by `SequentialMasker`, `JsonNodeDocumentMasker` and
  `XmlStringStaxMasker` to detect "masker changed the value". Leaf maskers must
  return the *same* instance when they do not change anything, not an equal-but-new string.
- **Thread safety**: leaf maskers and decorators are stateless once constructed and
  safe to reuse across threads. Document maskers are safe for concurrent calls with
  different inputs, but mutate their input for JSON — pass a deep copy if you need the original.

## Building

```bash
mvn clean install
```

GPG signing runs in the `verify` phase. For local development:

```bash
mvn -Dgpg.skip=true clean install
```

Single test class / method:

```bash
mvn test -Dtest=StringIbanMaskerTest
mvn test -Dtest=StringIbanMaskerTest#methodName
```

## Links

- Source: https://github.com/mjcro/masking
- Issues: https://github.com/mjcro/masking/issues
