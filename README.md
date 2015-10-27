# parsers

The GBIF parsers library provides:
 * Dictionary backed parsers for countries, language, taxon ranks, etc.
 * Parsers for dates and coordinates

## To build the project
```
mvn clean install
```

## Usage
Country parsing example:
```java
//get a Country by the defined enumaration
Country mexicoFromEnum = Country.MEXICO;

//get a Country from a String
ParseResult<Country> parsed = CountryParser.getInstance().parse("México");
if (parsed.getConfidence() == ParseResult.CONFIDENCE.DEFINITE){
  Country mexicoFromParser = parsed.getPayload();
  String iso2LetterCode = mexicoFromParser.getIso2LetterCode();
}

```

## Policies
 * Built as Java 6 artifact until the [IPT](https://github.com/gbif/ipt) upgrades its minimal Java version (see https://github.com/gbif/ipt/issues/1222).

## Documentations
 * [JavaDoc](shttp://gbif.github.io/parsers/apidocs/)