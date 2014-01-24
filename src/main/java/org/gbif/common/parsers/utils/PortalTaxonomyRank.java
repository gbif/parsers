package org.gbif.common.parsers.utils;

public enum PortalTaxonomyRank {

  SUPERKINGDOM(800, "superkingdom"),
  KINGDOM(1000, "kingdom"),
  SUBKINGDOM(1200, "subkingdom"),

  SUPERPHYLUM(1800, "superphylum"),
  PHYLUM(2000, "phylum"),
  SUBPHYLUM(2200, "subphylum"),

  SUPERCLASS(2800, "superclass"),
  CLASS(3000, "class"),
  SUBCLASS(3200, "subclass"),
  INFRACLASS(3350, "infraclass"),

  SUPERORDER(3800, "superorder"),
  ORDER(4000, "order"),
  SUBORDER(4200, "suborder"),
  INFRAORDER(4350, "infraorder"),
  PARVORDER(4400, "parvorder"),

  SUPERFAMILY(4500, "superfamily"),
  FAMILY(5000, "family"),
  SUBFAMILY(5500, "subfamily"),

  TRIBE(5600, "tribe"),
  SUBTRIBE(5700, "subtribe"),

  GENUS(6000, "genus"),
  NOTHOGENUS(6001, "nothogenus"),
  SUBGENUS(6500, "subgenus"),

  SECTION(6600, "section"),
  SUBSECTION(6700, "subsection"),

  SERIES(6800, "series"),
  SUBSERIES(6900, "subseries"),

  SPECIES_GROUP(6950, "species group"),
  SPECIES_SUBGROUP(6975, "species subgroup"),
  SPECIES(7000, "species"),
  NOTHOSPECIES(7001, "nothospecies"),
  SUBSPECIES(8000, "subspecies"),
  NOTHOSUBSPECIES(8001, "nothosubspecies"),
  VARIETY(8010, "variety"),
  NOTHOVARIETY(8011, "nothovariety"),
  FORM(8020, "form"),
  NOTHOFORM(8021, "nothoform"),
  BIOVAR(8030, "biovar"),
  SEROVAR(8040, "serovar"),
  CULTIVAR(8050, "cultivar"),
  PATHOVAR(8080, "pathovar"),


  INFRASPECIFIC(8090, "infraspecific"),
  ABERRATION(8100, "abberation"),
  MUTATION(8110, "mutation"),
  RACE(8120, "race"),
  CONFERSUBSPECIES(8130, "confersubspecies"),
  FORMASPECIALIS(8140, "formaspecialis"),
  HYBRID(8150, "hybrid"),

  UNKNOWN(0, "unranked");

  private final int id;
  private final String name;

  PortalTaxonomyRank(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
