package org.gbif.common.parsers.utils;

import org.gbif.api.vocabulary.Rank;

import java.util.HashMap;
import java.util.Map;

public class RankUtils {

  private static final Map<Rank, PortalTaxonomyRank> CLB_TO_PORTAL = new HashMap<Rank, PortalTaxonomyRank>();

  private RankUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  static {
    CLB_TO_PORTAL.put(Rank.KINGDOM, PortalTaxonomyRank.KINGDOM);
    CLB_TO_PORTAL.put(Rank.SUBKINGDOM, PortalTaxonomyRank.SUBKINGDOM);
    CLB_TO_PORTAL.put(Rank.SUPERPHYLUM, PortalTaxonomyRank.SUPERPHYLUM);
    CLB_TO_PORTAL.put(Rank.PHYLUM, PortalTaxonomyRank.PHYLUM);
    CLB_TO_PORTAL.put(Rank.SUBPHYLUM, PortalTaxonomyRank.SUBPHYLUM);
    CLB_TO_PORTAL.put(Rank.SUPERCLASS, PortalTaxonomyRank.SUPERCLASS);
    CLB_TO_PORTAL.put(Rank.CLASS, PortalTaxonomyRank.CLASS);
    CLB_TO_PORTAL.put(Rank.SUBCLASS, PortalTaxonomyRank.SUBCLASS);
    CLB_TO_PORTAL.put(Rank.SUPERORDER, PortalTaxonomyRank.SUPERORDER);
    CLB_TO_PORTAL.put(Rank.ORDER, PortalTaxonomyRank.ORDER);
    CLB_TO_PORTAL.put(Rank.SUBORDER, PortalTaxonomyRank.SUBORDER);
    CLB_TO_PORTAL.put(Rank.INFRAORDER, PortalTaxonomyRank.INFRAORDER);
    CLB_TO_PORTAL.put(Rank.SUPERFAMILY, PortalTaxonomyRank.SUPERFAMILY);
    CLB_TO_PORTAL.put(Rank.FAMILY, PortalTaxonomyRank.FAMILY);
    CLB_TO_PORTAL.put(Rank.SUBFAMILY, PortalTaxonomyRank.SUBFAMILY);
    CLB_TO_PORTAL.put(Rank.TRIBE, PortalTaxonomyRank.TRIBE);
    CLB_TO_PORTAL.put(Rank.SUBTRIBE, PortalTaxonomyRank.SUBTRIBE);
    CLB_TO_PORTAL.put(Rank.GENUS, PortalTaxonomyRank.GENUS);
    CLB_TO_PORTAL.put(Rank.SUBGENUS, PortalTaxonomyRank.SUBGENUS);
    CLB_TO_PORTAL.put(Rank.SECTION, PortalTaxonomyRank.SECTION);
    CLB_TO_PORTAL.put(Rank.SUBSECTION, PortalTaxonomyRank.SUBSECTION);
    CLB_TO_PORTAL.put(Rank.SERIES, PortalTaxonomyRank.SERIES);
    CLB_TO_PORTAL.put(Rank.SUBSERIES, PortalTaxonomyRank.SUBSERIES);
    CLB_TO_PORTAL.put(Rank.SPECIES, PortalTaxonomyRank.SPECIES);
    CLB_TO_PORTAL.put(Rank.INFRASPECIFIC_NAME, PortalTaxonomyRank.INFRASPECIFIC);
    CLB_TO_PORTAL.put(Rank.SUBSPECIES, PortalTaxonomyRank.SUBSPECIES);
    CLB_TO_PORTAL.put(Rank.VARIETY, PortalTaxonomyRank.VARIETY);
    CLB_TO_PORTAL.put(Rank.FORM, PortalTaxonomyRank.FORM);
    CLB_TO_PORTAL.put(Rank.CULTIVAR, PortalTaxonomyRank.CULTIVAR);
    CLB_TO_PORTAL.put(Rank.UNRANKED, PortalTaxonomyRank.UNKNOWN);
  }

  /**
   * Returns the Portal Rank Enumeration for the given CLB Rank.
   * A few are not mapped: Subvariety, Strain, Suprageneric, etc.
   *
   * @param clbRank the CLB Rank to lookup
   *
   * @return the corresponding Portal rank or null if it couldn't be found
   */
  public static PortalTaxonomyRank getPortalRank(Rank clbRank) {
    return CLB_TO_PORTAL.get(clbRank);
  }

}
