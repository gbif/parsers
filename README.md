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
### Maven artifact

```
<dependency>
  <groupId>org.gbif</groupId>
  <artifactId>gbif-parsers</artifactId>
  <version>[version]</version>
</dependency>
```

If you are not parsing URLs, you may want to exclude the large (10MB) ICU4J dependency, which is used to parse
internationalized domain names.

```
<dependency>
  <groupId>org.gbif</groupId>
  <artifactId>gbif-parsers</artifactId>
  <version>[version]</version>
  <exclusions>
    <exclusion>
      <groupId>com.ibm.icu</groupId>
      <artifactId>icu4j</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

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


## Policies
 * Built as Java 6 artifact until the [IPT](https://github.com/gbif/ipt) upgrades its minimal Java version (see https://github.com/gbif/ipt/issues/1222).

## Documentation
 * [JavaDoc](http://gbif.github.io/parsers/apidocs/)


[1] "Parser." Wikipedia: The Free Encyclopedia. Wikimedia Foundation, Inc. Retrieved June 28, 2016, from <https://en.wikipedia.org/wiki/Parsing#Parser>
