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
import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;

JsonNodeDocumentMasker masker = JsonNodeDocumentMasker.usingRulebook(
    new DefaultObjectFieldsRulebook()
);

String masked = masker.maskJsonString(
    "{\"email\":\"alice@example.com\",\"card\":\"4111111111111111\",\"cvv\":\"123\"}"
);
// {"email":"a***@example.com","card":"***1111","cvv":"***"}
```

### Mask an XML string

```java
import io.github.mjcro.masker.xml.XmlStringStaxMasker;
import io.github.mjcro.masker.rules.DefaultObjectFieldsRulebook;

XmlStringStaxMasker masker = XmlStringStaxMasker.usingRulebook(
    new DefaultObjectFieldsRulebook()
);
String masked = masker.applyMasking("<user><email>alice@example.com</email></user>");
```

### Mask HTTP headers

```java
import io.github.mjcro.masker.headers.HeadersPerNameMasker;
import io.github.mjcro.masker.rules.DefaultHttpHeadersRulebook;

HeadersPerNameMasker masker = HeadersPerNameMasker.usingRulebook(
    new DefaultHttpHeadersRulebook()
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
reacts to every category. Shipped defaults:

- `DefaultObjectFieldsRulebook` — JSON/XML field names (`cvv`, `iban`, `card`,
  `email`, `phone`, names, government IDs, ...).
- `DefaultHttpHeadersRulebook` — HTTP headers (`authorization`, `cookie`,
  `x-api-key`, `referer`, ...).
- `SimpleRulebook` — plain container for custom rules, no subclassing required.

### Document-level maskers

| Class                              | Input                               | Notes                                                                                  |
|------------------------------------|-------------------------------------|----------------------------------------------------------------------------------------|
| `JsonNodeDocumentMasker`           | Jackson `JsonNode` or JSON string   | Walks the tree, mutates in place. Handles `parent.child` compound names and primitive arrays. |
| `XmlStringStaxMasker`              | XML string                          | Streaming StAX rewriter with hardened DTD/external-entity settings. Masks element text and attributes. |
| `HeadersPerNameMasker`             | `Map<String, List<String>>`         | Per-name lookup (case-insensitive) with a default-masker fallback.                     |

## Custom rulebook example

```java
import io.github.mjcro.masker.rules.Rulebook;
import io.github.mjcro.masker.rules.SimpleRulebook;
import io.github.mjcro.masker.strings.*;
import java.util.List;

Rulebook rb = new SimpleRulebook(
    null, // UTF-8
    StringLongTruncationMasker.DEFAULT, // default
    List.of(StringIbanMasker.DEFAULT.asInlineMasker()), // inline
    List.of(
        Rulebook.tuple(StringFullMasker.DEFAULT, "cvv", "pin"),
        Rulebook.tuple(StringCardPanMasker.DEFAULT, "card", "pan")
    ),
    List.of(
        Rulebook.tuple(StringEmailMasker.DEFAULT, "email"),
        Rulebook.tuple(PhoneNumberMasker.DEFAULT, "phone")
    )
);
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
