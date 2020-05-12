package org.gbif.common.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UrlParserTest {

  @Test
  public void testParse() throws Exception {
    assertNull(UrlParser.parse(null));
    assertNull(UrlParser.parse(""));
    assertNull(UrlParser.parse(" "));
    assertNull(UrlParser.parse("-"));
    assertNull(UrlParser.parse("tim.1png"));
    assertNull(UrlParser.parse("images/logo.gif"));

    assertEquals("http://tim.png", UrlParser.parse("tim.png").toString());
    assertEquals("http://www.gbif.org", UrlParser.parse("www.gbif.org").toString());
    assertEquals("http://www.gbif.org/logo.png", UrlParser.parse(" http://www.gbif.org/logo.png").toString());
    assertEquals("http://www.gbif.org/logo.png", UrlParser.parse("www.gbif.org/logo.png").toString());
    assertEquals("https://www.gbif.org/logo.png", UrlParser.parse(" https://www.gbif.org/logo.png").toString());
    assertEquals("ftp://www.gbif.org/logo.png", UrlParser.parse(" ftp://www.gbif.org/logo.png").toString());
    assertEquals("http://www.gbif.org/image?id=12&format=gif,jpg", UrlParser.parse("http://www.gbif.org/image?id=12&format=gif,jpg").toString());
    assertEquals("https://xn----8sbahmlpvellw0ag7lzb.xn--p1ai", UrlParser.parse("https://xn----8sbahmlpvellw0ag7lzb.xn--p1ai").toString());

    // Unicode characters in the path
    assertEquals("https://ru.wikipedia.org/wiki/Биоразнообразие", UrlParser.parse("https://ru.wikipedia.org/wiki/Биоразнообразие").toString());

    // IDNs (currently normalized)
    assertEquals("http://xn--sknetrafiken-ucb.se/", UrlParser.parse("http://skånetrafiken.se/").toString());
    assertEquals("https://xn----8sbahmlpvellw0ag7lzb.xn--p1ai/", UrlParser.parse("https://музей-мартьянова.рф/").toString());
    assertEquals("https://xn----8sbahmlpvellw0ag7lzb.xn--p1ai/музей", UrlParser.parse("https://музей-мартьянова.рф/музей").toString());

    // Although WHATWG says _ is allowed in domains, and the Galimatias library says it follows the spec, it still
    // rejects them.  (Java also rejects them, following other specifications of URLs.)
    // See https://github.com/gbif/parsers/issues/19
    // assertEquals("http://dep_bio.pnzgu.ru/Gerbariy_im_I_I_Sprygina", UrlParser.parse("http://dep_bio.pnzgu.ru/Gerbariy_im_I_I_Sprygina").toString());
  }

  @Test
  public void testParseUriList() throws Exception {
    assertEquals(0, UrlParser.parseUriList(null).size());
    assertEquals(0, UrlParser.parseUriList("").size());
    assertEquals(0, UrlParser.parseUriList(" ").size());
    assertEquals(0, UrlParser.parseUriList("-").size());

    assertEquals(1, UrlParser.parseUriList("http://gbif.org/logo.png").size());
    assertEquals(1, UrlParser.parseUriList(" http://gbif.org/logo.png").size());
    assertEquals(1, UrlParser.parseUriList("www.gbif.org/logo.png").size());
    assertEquals(1, UrlParser.parseUriList("www.gbif.org/image?id=12").size());
    assertEquals(1, UrlParser.parseUriList("http://www.gbif.org/image?id=12").size());
    assertEquals(1, UrlParser.parseUriList("http://www.gbif.org/image?id=12&format=gif,jpg").size());

    assertEquals(2, UrlParser.parseUriList("http://gbif.org/logo.png, http://gbif.org/logo2.png").size());
    assertEquals(2, UrlParser.parseUriList("http://gbif.org/logo.png; http://gbif.org/logo2.png").size());
    assertEquals(2, UrlParser.parseUriList("http://gbif.org/logo.png | http://gbif.org/logo2.png").size());
    assertEquals(2, UrlParser.parseUriList("http://gbif.org/logo.png |#DELIMITER#| http://gbif.org/logo2.png").size());

    assertEquals(3, UrlParser.parseUriList("http://gbif.org/logo.png, http://gbif.org/logo2.png, http://gbif.org/logo3.png").size());

    assertEquals(2, UrlParser.parseUriList("imagens3.jbrj.gov.br/fsi/server?type=image&source=rb/0/13/11/84/00131184.jpg|imagens3.jbrj.gov.br/fsi/server?type=image&source=rb/0/13/11/84/00131184-1.jpg").size());
  }
}
