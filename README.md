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
The date parsing is based on the [Java 8 DateTime API](http://www.oracle.com/technetwork/articles/java/jf14-date-time-2125367.html) but in order to support Java 6 and 7, this version is implemented using the [ThreeTen Backport
](http://www.threeten.org/threetenbp/) project.

Note that the TextDateParser returns a [TemporalAccessor](http://www.threeten.org/threetenbp/apidocs/org/threeten/bp/temporal/TemporalAccessor.html), an high-level and abstracted representation
of a date/time.

```java
TextDateParser dateParser = new TextDateParser();
ParseResult<TemporalAccessor> ta = dateParser.parse("2nd jan. 1999");

LocalDate localDate = LocalDate.from(ta.getPayload());

// or using the date parts
ta = dateParser.parse("1999", "jan.", "2");
localDate = LocalDate.from(ta.getPayload());
```


## Policies
 * Built as Java 6 artifact until the [IPT](https://github.com/gbif/ipt) upgrades its minimal Java version (see https://github.com/gbif/ipt/issues/1222).

## Documentation
 * [JavaDoc](http://gbif.github.io/parsers/apidocs/)


[1] "Parser." Wikipedia: The Free Encyclopedia. Wikimedia Foundation, Inc. Retrieved June 28, 2016, from <https://en.wikipedia.org/wiki/Parsing#Parser>