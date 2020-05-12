package org.gbif.common.parsers;

import org.gbif.api.model.common.MediaObject;
import org.gbif.api.vocabulary.MediaType;

import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MediaParserTest {
  private final MediaParser parser = MediaParser.getInstance();

  @Test
  public void testDetectType() throws Exception {
    assertEquals(MediaType.StillImage, parser.detectType(buildMO("image/jp2", "abies_alba.jp2")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO("image/jpeg", null)).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO("image/jpg", null)).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "abies_alba.jp2")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "abies_alba.jpg")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "abies_alba.JPG")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "abies_alba.JPeg")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "abies_alba.TIFF")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "http://imagens3.jbrj.gov.br/fsi/server?type=image&source=rb/1/37/81/25/01378125.jpg")).getType());
    assertEquals(MediaType.StillImage, parser.detectType(buildMO(null, "http://rrrcn.wildlifemonitoring.ru/get_photo.php?id=119797")).getType());

    assertEquals(MediaType.Sound, parser.detectType(buildMO(null, "abies_alba.mp3")).getType());
    assertEquals(MediaType.Sound, parser.detectType(buildMO(null, "abies_alba.flac")).getType());
    assertEquals(MediaType.Sound, parser.detectType(buildMO(null, "abies_alba.ogg")).getType());
  }

  @Test
  public void testParseMimeType() throws Exception {
    assertNull(parser.parseMimeType((String) null));
    assertEquals("image/jp2", parser.parseMimeType(" image/jp2\t"));
    assertEquals("image/jpg", parser.parseMimeType("image/jpg"));
    assertEquals("image/jpeg", parser.parseMimeType("image/jpeg"));
    assertEquals("image/jpg", parser.parseMimeType("image/JPG"));
    assertNull(parser.parseMimeType("JPG"));
    assertNull(parser.parseMimeType("gif"));
    assertNull(parser.parseMimeType("tiff"));

    assertNull(parser.parseMimeType((URI) null));
    assertEquals("image/jp2", parser.parseMimeType(URI.create("abies_alba.jp2")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("abies_alba.jpg")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("abies_alba.jpeg")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("abies_alba.JPG")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("abies_alba.JPG")));
    assertEquals("image/gif", parser.parseMimeType(URI.create("abies_alba.gif")));
    assertEquals("image/tiff", parser.parseMimeType(URI.create("abies_alba.tiff")));
    assertEquals("audio/mpeg", parser.parseMimeType(URI.create("abies_alba.mp3")));
    assertEquals("video/mpeg", parser.parseMimeType(URI.create("abies_alba.mpg")));

    // we default to an html link
    assertEquals("text/html", parser.parseMimeType(URI.create("http://www.gbif.org/image?id=12")));
    assertEquals("text/html", parser.parseMimeType(URI.create("http://arctos.database.museum/MediaSearch.cfm?action=search")));
    assertEquals("text/html", parser.parseMimeType(URI.create("http://arctos.database.museum/MediaSearch.php?action=search")));
    assertEquals("text/html", parser.parseMimeType(URI.create("http://arctos.database.museum/MediaSearch.pl?action=search")));

    // Audio.
    assertNull(parser.parseMimeType("mp3"));
    assertNull(parser.parseMimeType("mpg"));

    assertEquals("audio/ogg", parser.parseMimeType("audio/ogg"));
    assertEquals("application/ogg", parser.parseMimeType("application/ogg"));
    assertEquals("audio/webm", parser.parseMimeType("audio/webm"));
    assertEquals("audio/mp4", parser.parseMimeType("audio/mp4"));
    assertEquals("audio/vnd.wave", parser.parseMimeType("audio/wav"));

    assertEquals("audio/mpeg", parser.parseMimeType("audio/mp3"));
    assertEquals("audio/mpeg", parser.parseMimeType("audio/mpeg3"));

    // Known URL patterns
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("http://www.unimus.no/felles/bilder/web_hent_bilde.php?id=13080366&type=jpeg")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("http://www.jacq.org/image.php?filename=99618&method=europeana")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("https://images.ala.org.au/image/proxyImageThumbnailLarge?imageId=ffff9990-c4a7-4431-803f-3c5f14bd94ba")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("http://russiabirds.wildlifemonitoring.ru/get_photo.php?id=99981")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("http://rrrcn.wildlifemonitoring.ru/get_photo.php?id=99999")));
    assertEquals("image/jpeg", parser.parseMimeType(URI.create("http://procyon.acadiau.ca/ecsmith/cgi-bin/image.cgi?NSPM044956P,jpeg")));
    assertEquals("audio/vnd.wave", parser.parseMimeType(URI.create("http://www.biologie.uni-ulm.de/cgi-bin/perl/sound.pl?sid=T&objid=99999")));
    assertEquals("audio/mpeg", parser.parseMimeType(URI.create("https://dofbasen.dk/sound_proxy.php?referer=gbif&mode=o&snd=99881321.mp3&raw=1")));
  }

  @Test
  public void testParseHtmlMediaLink() throws Exception {
    MediaObject mo = buildMO(null, "http://www.gbif.org/image?id=12");
    parser.detectType(mo);
    assertNull(mo.getFormat());
    assertNull(mo.getIdentifier());
    assertEquals("http://www.gbif.org/image?id=12", mo.getReferences().toString());
    assertNull(mo.getType());
  }

  @Test
  public void testParseKnownImageMediaLink() throws Exception {
    MediaObject mo = buildMO(null, "http://rrrcn.wildlifemonitoring.ru/get_photo.php?id=119797");
    parser.detectType(mo);
    assertEquals(MediaType.StillImage, mo.getType());
    assertEquals("http://rrrcn.wildlifemonitoring.ru/get_photo.php?id=119797", mo.getIdentifier().toString());
    assertNull(mo.getReferences());
    assertEquals("image/jpeg", mo.getFormat());
  }

  private MediaObject buildMO(String format, String uri) {
    MediaObject mo = new MediaObject();

    mo.setType(null);
    mo.setFormat(format);
    mo.setIdentifier(uri == null ? null : URI.create(uri));

    return mo;
  }
}
