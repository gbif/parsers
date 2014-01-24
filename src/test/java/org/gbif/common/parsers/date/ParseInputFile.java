package org.gbif.common.parsers.date;

import org.gbif.common.parsers.ParseResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;

/**
 * A small utility to allow real world testing.
 * Takes a tab delimited input file of day \t month \t year
 * and outputs 7 columns of input fields, parsed fields and
 * a constructed date.
 * There is the complete distinct verbatim day,month,year fields
 * found in raw_occurrence_record table in the src/test/resources
 *
 * @author tim
 */
public class ParseInputFile {

  /**
   * @param args inputFile outputFile skipCompleteRecords
   */
  public static void main(String[] args) throws IOException {
    BufferedReader r = null;
    if (args[0].endsWith(".gz")) {
      r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
    } else {
      r = new BufferedReader(new FileReader(args[0]));
    }
    BufferedWriter w = new BufferedWriter(new FileWriter(args[1]));
    w.write("D-in\tM-in\tY-in\tD-out\tM-out\tY-out\tDate\n");
    boolean skip = false;
    try {
      skip = Boolean.parseBoolean(args[2]);
    } catch (RuntimeException e) {
    }
    Pattern tab = Pattern.compile("\t");
    String line = null;
    int lineCount = 0;
    int ymdCount = 0;
    int dateCount = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    while ((line = r.readLine()) != null) {
      String[] dmy = tab.split(line);
      String d = null;
      String m = null;
      String y = null;
      try {
        d = dmy[0];
      } catch (Exception e) {
      }
      ;
      try {
        m = dmy[1];
      } catch (Exception e) {
      }
      ;
      try {
        y = dmy[2];
      } catch (Exception e) {
      }
      ;

      YearMonthDay ymd = DateParseUtils.normalize(y, m, d);
      ParseResult<Date> pr = DateParseUtils.parse(ymd.getYear(), ymd.getMonth(), ymd.getDay());
      Date date = null;
      if (ParseResult.STATUS.SUCCESS == pr.getStatus()) {
        date = pr.getPayload();
      }

      if (skip && date != null && StringUtils.isNotBlank(ymd.getDay()) && StringUtils.isNotBlank(ymd.getMonth())
          && StringUtils.isNotBlank(ymd.getYear())) {
        dateCount++;
        ymdCount++;

      } else {
        w.write(d + "\t");
        w.write(m + "\t");
        w.write(y + "\t");
        if (ymd.getDay() != null) w.write(ymd.getDay());
        w.write("\t");
        if (ymd.getMonth() != null) w.write(ymd.getMonth());
        w.write("\t");
        if (ymd.getYear() != null) w.write(ymd.getYear());
        w.write("\t");

        if (!ymd.representsNull()) ymdCount++;

        if (ParseResult.STATUS.SUCCESS == pr.getStatus()) {
          w.write(sdf.format(date));
          dateCount++;
        }
        w.write("\n");
      }


      lineCount++;
      if (lineCount % 10000 == 0) {
        System.out.println("Processed[" + lineCount + "], ymdFound[" + ymdCount + "], dateFound[" + dateCount + "]");
      }
    }
    System.out.println("Processed[" + lineCount + "], ymdFound[" + ymdCount + "], dateFound[" + dateCount + "]");
    w.close();
    r.close();
  }
}
