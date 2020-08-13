# parsers

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
//get a Country by the defined enumeration
Country mexicoFromEnum = Country.MEXICO;

//get a Country from a String
ParseResult<Country> parsed = CountryParser.getInstance().parse("México");
if (parsed.getConfidence() == ParseResult.CONFIDENCE.DEFINITE){
  Country mexicoFromParser = parsed.getPayload();
  String iso2LetterCode = mexicoFromParser.getIso2LetterCode();
}

```

## Date parsing

```java
//make sure to use org.threeten.bp package
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAccessor;

//...

TemporalParser dateParser = DateParsers.defaultTemporalParser();
ParseResult<TemporalAccessor> ta = dateParser.parse("2nd jan. 1999");

LocalDate localDate = LocalDate.from(ta.getPayload());

// or using the date parts
ta = dateParser.parse("1999", "jan.", "2");
localDate = LocalDate.from(ta.getPayload());
```
For more information and details about the date parsing see the [Date Parsing Documentation](/assets/DateParsingDocumentation.md).

//or ONLY use the provided hint to parse date.
//If the provided date format failed, then stop and return failure
ta = dateParser.parse("1980-1-0", DateFormatHint.YMD);

//This function will try all date formats to parse date, 
//If failed with ambiguous warning, then FORCE using the provided date formats.
ta = dateParser.parse("1980-1-0", new DateFormatHint[]{DateFormatHint.US_MDYZ});



## Policies
 * Built as Java 8 artifact
 
## Documentation
 * [JavaDoc](https://gbif.github.io/parsers/apidocs/)

[1] "Parser." Wikipedia: The Free Encyclopedia. Wikimedia Foundation, Inc. Retrieved June 28, 2016, from <https://en.wikipedia.org/wiki/Parsing#Parser>
