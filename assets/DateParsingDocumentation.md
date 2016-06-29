# Date Parsing - Documentation and Design Notes

## Documentation
### Main usage
The main purpose of these date parsing classes is to parse a String into a representation of a date when
the date format used is unknown. It is not intended to be a replacement for [SimpleDateFormat](http://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html)
or Java 8 [DateTimeFormatter](http://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) and it is not based
on natural language like [Natty](http://natty.joestelmach.com/).

### Threeten Backport and Java 8 Date/Time API
The date parsing is based on the [Java 8 DateTime API](http://www.oracle.com/technetwork/articles/java/jf14-date-time-2125367.html) but in order to support Java 6 and 7, it is
implemented using the [ThreeTen Backport](http://www.threeten.org/threetenbp/) project.
ThreeTen Backport uses its own namespace `org.threeten.bp` which allows it to run on Java 6, 7 and 8.

The use of ThreeTen Backport must be considered temporary since the project will be moved to Java 8 code as soon as possible.
Classes like `TemporalAccessor` and all its implementation will keep the same name but will use the `java.time.temporal` namespace.

### TemporalAccessor and java.util.Date
The date parsing classes are all working with a [TemporalAccessor](http://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalAccessor.html)
instance to represent a date. The implementations of this interface include: [LocalDate](http://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html),
[OffsetDateTime](http://docs.oracle.com/javase/8/docs/api/java/time/OffsetDateTime.html), partial date representation like
[YearMonth](http://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html) and [Year](http://docs.oracle.com/javase/8/docs/api/java/time/Year.html).

For backward compatibility it is possible to transform a `TemporalAccessor` into a `java.util.Date`.
See [TemporalAccessorUtils](http://gbif.github.io/parsers/apidocs/org/gbif/common/parsers/date/TemporalAccessorUtils.html) for utility methods and
[AtomizedLocalDate](http://gbif.github.io/parsers/apidocs/org/gbif/common/parsers/date/AtomizedLocalDate.html) for simple operations.

### Entry points
There is 2 main methods in [TextDateParser](http://gbif.github.io/parsers/apidocs/org/gbif/common/parsers/date/TextDateParser.html) to parse dates represented in String.
They both return a `ParseResult<TemporalAccessor>` object.
```
- parse(String input)
- parse(String year, String month, String day)
```

## Design and implementation details


Design overview
![Design](./date_parsing_design.png)


### TextDateParser Workflow

In order to use the right parser, the `TextDateParser` will use a regular expression to determine if the input String contains only
numerical characters and separators or if it also include some letters. the presence of letters may indicate the month
is written in text as opposed to its numerical value.

### ThreeTenNumericalDateParser
The `ThreeTenNumericalDateParser` contains a predefined set of date formats built on top of the [Threeten Backport](http://www.threeten.org/threetenbp/) project.
The set of predefined date formats can be seen as a set of 3 different types of format:

 * Nom ambiguous format e.g. ISO based date formats
 * Possibly ambiguous format with preference e.g. 2.6.2016 as used Germany
 * Possibly ambiguous format e.g. 6/2/2016

The difference between "possibly ambiguous format with preference" and simply "possibly ambiguous format" appears when 2 patterns
can be applied to the provided String. In case of "with preference" we will prefer a format over the other and return the confidence
`ParseResult.CONFIDENCE.PROBABLE`. For possibly ambiguous formats without a preference the parsing will fail in case 2 patterns can be matched
unless the matches represent the excat same date e.g. 2/2/2016.


By default, this parser will not parse dates containing the year expressed as 2 digits. This feature can be enabled by using
the static method `newInstance(Year baseYear)`.

It is also possible to give a hint to the parser when the order or the granularity of the date components is known.
The enumeration [DateFormatHint](http://gbif.github.io/parsers/apidocs/org/gbif/common/parsers/date/DateFormatHint.html)
contains the possible values.

```java
NumericalDateParser NUMERICAL_PARSER = DateParsers.defaultNumericalDateParser();

//this ParseResult will not be successful since this date is ambiguous
ParseResult<TemporalAccessor> ta = NUMERICAL_PARSER.parse("02/01/1999");

//but if we provide a DateFormatHint, we can get the expected result
ta = NUMERICAL_PARSER.parse("02/01/1999", DateFormatHint.MDY);
// or
ta = NUMERICAL_PARSER.parse("02/01/1999", DateFormatHint.DMY);
```

### TextualMonthDateTokenizer
This class is mostly use to break a string into different tokens.

### DatePartsNormalizer
This class is used to transform strings representing year, month and day and return the corresponding Integer as
[NormalizedYearMonthDay](http://gbif.github.io/parsers/apidocs/org/gbif/common/parsers/date/DatePartsNormalizer.NormalizedYearMonthDay.html).

```java
private static DatePartsNormalizer NORMALIZER = DatePartsNormalizer.newInstance();

DatePartsNormalizer.NormalizedYearMonthDay result = NORMALIZER.normalize("1975", "jan", "1");
// equals Integer 1975
result.getYear();
// equals Integer 1
result.getMonth();
// equals Integer 1
result.getDay();
```
