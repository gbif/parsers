# GBIF Parsers

> A parser is a software component that takes input data (frequently text) and builds a data structure [1].

The GBIF parsers library provides:
* Dictionary backed parsers for countries, language, taxon ranks, etc.
* Parsers for dates and coordinates

## To build the project
```
mvn clean install
```

## Usage
### Country parsing
```java
// Get a Country by the defined enumeration
Country mexicoFromEnum = Country.MEXICO;

// Get a Country from a String
ParseResult<Country> parsed = CountryParser.getInstance().parse("México");
if (parsed.getConfidence() == ParseResult.CONFIDENCE.DEFINITE) {
  Country mexicoFromParser = parsed.getPayload();
  String iso2LetterCode = mexicoFromParser.getIso2LetterCode();
}
```

## Date parsing

```java
TemporalParser dateParser = DateParsers.defaultTemporalParser();
ParseResult<TemporalAccessor> ta = dateParser.parse("2nd jan. 1999");

LocalDate localDate = LocalDate.from(ta.getPayload());

// or using the date parts
ta = dateParser.parse("1999", "jan.", "2");
localDate = LocalDate.from(ta.getPayload());

// or use ONLY the provided format to parse the date
// other formats will return a parse failure with possible dates
ta = dateParser.parse("1980-1-0", DateComponentOrdering.YMD);

// or try all date formats, but use the provided format(s) to choose between ambiguous formats.
ta = dateParser.parse("12/08/2020", DateComponentOrdering.DMY_FORMATS);
```
For more information and details about the date parsing see the [Date Parsing Documentation](/assets/DateParsingDocumentation.md).

## Policies
 * Built as Java 8 artifact

## Documentation
 * [JavaDoc](https://gbif.github.io/parsers/apidocs/)

[1] "Parser." Wikipedia: The Free Encyclopedia. Wikimedia Foundation, Inc. Retrieved June 28, 2016, from <https://en.wikipedia.org/wiki/Parsing#Parser>
