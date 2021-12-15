/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.common.parsers.geospatial;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for dealing with CellId.
 */
public class CellIdUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellIdUtils.class);

  private static final int MAX_LATITUDE = 90;
  private static final int MIN_LATITUDE = -MAX_LATITUDE;
  private static final int MAX_LONGITUDE = 180;
  private static final int MIN_LONGITUDE = -MAX_LONGITUDE;

  private CellIdUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  /**
   * Determines the cell id for the Lat / Long provided.
   *
   * @param latitude  Which may be null
   * @param longitude Which may be null
   *
   * @return The cell id for the Lat Long pair
   *
   * @throws UnableToGenerateCellIdException
   *          Should the lat long be null or invalid
   */
  public static int toCellId(Double latitude, Double longitude) throws UnableToGenerateCellIdException {
    LOGGER.debug("Getting cell for [{},{}]", latitude, longitude);

    if (latitude == null || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE || longitude < MIN_LONGITUDE
        || longitude > MAX_LONGITUDE) {
      throw new UnableToGenerateCellIdException(
        "Latitude[" + latitude + "], Longitude[" + longitude + "] cannot be converted to a cell id");
    } else {
      int la = getCellIdFor(latitude);
      int lo = getMod360CellIdFor(longitude);
      return la + lo;
    }
  }

  /**
   * Get mod 360 cell id.
   */
  public static int getMod360CellIdFor(double longitude) {
    return Double.valueOf(Math.floor(longitude + MAX_LONGITUDE)).intValue();
  }

  /**
   * Get cell id.
   */
  public static int getCellIdFor(double latitude) {
    return Double.valueOf(Math.floor(latitude + MAX_LATITUDE)).intValue() * 360;
  }

  /**
   * Returns true if the supplied cell id lies in the bounding box demarcated by the min and max cell ids supplied.
   */
  public static boolean isCellIdInBoundingBox(int cellId, int minCellId, int maxCellId) throws Exception {
    return cellId >= minCellId && cellId <= (maxCellId - 361) && (cellId % 360) >= (minCellId % 360)
           && (cellId % 360) <= ((maxCellId - 361) % 360);
  }

  /**
   * Determines the centi cell id for the given values
   *
   * @param latitude  Which may be null
   * @param longitude Which may be null
   *
   * @return The centi cell id within the cell for the lat long
   *
   * @throws UnableToGenerateCellIdException
   *          Shoudl the lat long be null or invalid
   */
  public static int toCentiCellId(Double latitude, Double longitude) throws UnableToGenerateCellIdException {
    if (latitude == null || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE || longitude < MIN_LONGITUDE
        || longitude > MAX_LONGITUDE) {
      throw new UnableToGenerateCellIdException(
        "Latitude[" + latitude + "], Longitude[" + longitude + "] cannot be " + "converted to a centi cell id");
    } else {

      //get decimal value for up to 4 decimal places
      //17.2-> 172000 -> 2000
      int la = Math.abs((int) (latitude * 10000) % 10000);
      if (latitude < 0) la = 10000 - la;
      la = (la / 1000) % 10;
      int lo = Math.abs((int) (longitude * 10000) % 10000);
      if (longitude < 0) lo = 10000 - lo;
      lo = (lo / 1000) % 10;

      int centiCellId = (la * 10) + lo;
      return Math.abs(centiCellId);
    }
  }

  /**
   * Returns the box of the given cell This may require some more work to avoid divide rounding errors
   *
   * @param cellId To return the lat long box of
   *
   * @return The box
   */
  public static LatLngBoundingBox toBoundingBox(int cellId) {
    int longitude = (cellId % 360) - MAX_LONGITUDE;
    int latitude = MIN_LATITUDE;
    if (cellId > 0) {
      latitude = Double.valueOf(Math.floor(cellId / 360)).intValue() - MAX_LATITUDE;
    }
    return new LatLngBoundingBox(longitude, latitude, longitude + 1, latitude + 1);
  }

  /**
   * Returns the box of the given cell and centi cell An attempt has been made to avoid rounding errors with doubles,
   * but may need revisited
   *
   * @param cellId      To return the lat long box of
   * @param centiCellId within the box
   *
   * @return The box
   */
  public static LatLngBoundingBox toBoundingBox(int cellId, int centiCellId) {
    int longitudeX10 = 10 * ((cellId % 360) - MAX_LONGITUDE);
    int latitudeX10 = -900;
    if (cellId > 0) {
      latitudeX10 = 10 * (Double.valueOf(Math.floor(cellId / 360)).intValue() - MAX_LATITUDE);
    }

    double longOffset = (centiCellId % 10);
    double latOffset = 0;
    if (centiCellId > 0) {
      latOffset = centiCellId / 10;
    }

    double minLatitude = (latitudeX10 + latOffset) / 10;
    double minLongitude = (longitudeX10 + longOffset) / 10;
    double maxLatitude = (latitudeX10 + latOffset + 1) / 10;
    double maxLongitude = (longitudeX10 + longOffset + 1) / 10;
    return new LatLngBoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude);
  }

  /**
   * Gets the list of cells that are enclosed within the bounding box. Cells that are partially enclosed are returned
   * also
   * TODO implement this properly, the current version will include cells that are partially included on the bottom and
   * left, but not the top and right
   *
   * @return The cells that are enclosed by the bounding box
   *
   * @throws UnableToGenerateCellIdException
   *          if the lat longs are invalid
   */
  public static Set<Integer> getCellsEnclosedBy(double minLat, double maxLat, double minLong, double maxLong)
    throws UnableToGenerateCellIdException {
    if (minLat < MIN_LATITUDE) minLat = MIN_LATITUDE;
    if (maxLat > MAX_LATITUDE) maxLat = MAX_LATITUDE;
    if (minLong < MIN_LONGITUDE) minLong = MIN_LONGITUDE;
    if (maxLong > MAX_LONGITUDE) maxLong = MAX_LONGITUDE;

    LOGGER.debug("Establishing cells enclosed by: {}:{}   {}:{}", new Object[] {minLat, maxLat, minLong, maxLong});

    int lower = toCellId(minLat, minLong);
    int upper = toCellId(maxLat, maxLong);

    LOGGER.debug("Unprocessed cells: {} -> {}", lower, upper);

    // if the BB upper right corner is on a grid, then it needs flagged
    if (Math.ceil(maxLong) == Math.floor(maxLong)) {
      LOGGER.debug("Longitude lies on a boundary");
      upper -= 1;
    }
    if (Math.ceil(maxLat) == Math.floor(maxLat)) {
      LOGGER.debug("Latitude lies on a boundary");
      upper -= 360;
    }

    LOGGER.debug("Getting cells contained in {} to {}", lower, upper);

    int omitLeft = lower % 360;
    int omitRight = upper % 360;
    if (omitRight == 0) omitRight = 360;
    Set<Integer> cells = new HashSet<Integer>();
    for (int i = lower; i <= upper; i++) {
      if (i % 360 >= omitLeft && i % 360 <= omitRight) {
        cells.add(i);
      }
    }
    return cells;
  }

  /**
   * Return a min cell id and a max cell id for this bounding box.
   *
   * @return the minCellId in int[0] and maxCellId in int[1]
   */
  public static int[] getMinMaxCellIdsForBoundingBox(double minLongitude, double minLatitude, double maxLongitude,
    double maxLatitude) throws UnableToGenerateCellIdException {

    int minCellId = toCellId(minLatitude, minLongitude);
    int maxCellId = toCellId(maxLatitude, maxLongitude);

    if (Math.ceil(maxLatitude) == Math.floor(maxLatitude) && Math.ceil(maxLongitude) == Math.floor(maxLongitude)
        && maxLongitude != 180f && maxLatitude != 90f && maxCellId > 0) {

      //the maxLongitude,maxLatitude point is on a cell intersection, hence the maxCellId should be
      // -361 the maxCellId CellIdUtils will give us i.e. the cell that is
      // 1 below and 1 to the left of the cell id retrieved
      //unless it is the 64799 cell.
      maxCellId = maxCellId - 361;
    }

    return new int[] {minCellId, maxCellId};
  }

  /**
   * Creates a bounding box for the list of unordered cell ids.
   *
   * @return a LatLngBoundingBox that encapsulates this list of cell ids.
   */
  public static LatLngBoundingBox getBoundingBoxForCells(List<Integer> cellIds) {
    if (cellIds.isEmpty()) return null;
    //first cell - gives the minLat
    //double minLatitude = toBoundingBox(cellIds.get(0)).minLat;
    //last cell - give the maxLat
    //double maxLatitude = toBoundingBox(cellIds.get(cellIds.size()-1)).maxLat;
    int minLatitudeCellId = cellIds.get(0);
    int maxLatitudeCellId = cellIds.get(cellIds.size() - 1);

    int minLongitudeCellId = cellIds.get(0);
    int maxLongitudeCellId = cellIds.get(cellIds.size() - 1);
    //the min cell (id % 360) - gives min longitude
    //the max cell (id % 360) - gives max longitude
    for (Integer cellId : cellIds) {

      Integer cellIdMod360 = cellId % 360;
      if (cellIdMod360 < (minLongitudeCellId % 360)) minLongitudeCellId = cellIdMod360;
      if (cellIdMod360 > (maxLongitudeCellId % 360)) maxLongitudeCellId = cellIdMod360;

      if (cellId < minLatitudeCellId) minLatitudeCellId = cellId;
      if (cellId > maxLatitudeCellId) maxLatitudeCellId = cellId;
    }
    double minLongitude = toBoundingBox(minLongitudeCellId).minLong;
    double minLatitude = toBoundingBox(minLatitudeCellId).minLat;
    double maxLongitude = toBoundingBox(maxLongitudeCellId).maxLong;
    double maxLatitude = toBoundingBox(maxLatitudeCellId).maxLat;

    return new LatLngBoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude);
  }

  /**
   * Returns the cell id and centi cell id for the supplied bounding box, Returning null if the supplied bounding box
   * doesnt enclose a single cell. If the bounding box encloses a single cell but not a centi cell, a Integer[] of
   * length 1 is returned with containing the cell id. Otherwise a Integer array of length 2, with Integer[0] being the
   * cell id, Integer[1] being the centi cell.
   */
  public static Integer[] getCentiCellIdForBoundingBox(double minLongitude, double minLatitude, double maxLongitude,
    double maxLatitude) throws UnableToGenerateCellIdException {

    //int[] maxMinCellIds = getMinMaxCellIdsForBoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude);
    //if(maxMinCellIds==null || (maxMinCellIds[0]!=maxMinCellIds[1]))
    //	return null;

    //Integer cellId = maxMinCellIds[0];

    //int[] maxMinCellIds = getMinMaxCellIdsForBoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude);

    if (!isBoundingBoxCentiCell(minLongitude, minLatitude, maxLongitude, maxLatitude)) {
      return null;
    }

    //ascertain whether bounding box is 0.1 by 0.1
    //if(isBoundingBoxCentiCell(minLongitude, minLatitude, maxLongitude, maxLatitude)){

    int[] maxMinCellIds = getMinMaxCellIdsForBoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude);
    Integer cellId = maxMinCellIds[0];

    int minCentiCell = toCentiCellId(minLatitude, minLongitude);
    int maxCentiCell = toCentiCellId(maxLatitude, maxLongitude);

    double maxLongitude10 = maxLongitude * 10;
    double maxLatitude10 = maxLatitude * 10;

    if (Math.ceil(maxLatitude10) == Math.floor(maxLatitude10) && Math.ceil(maxLongitude10) == Math.floor(maxLongitude10)
        && maxCentiCell > 0) {

      //the maxLongitude,maxLatitude point is on a centi cell intersection, hence the maxCentiCellId should be
      // maxCentiCellId-11 i.e. the cell that is
      // 1 below and 1 to the left of the centi cell id retrieved
      //unless it is the 100 centi cell.
      if (maxCentiCell > minCentiCell) {
        maxCentiCell = maxCentiCell - 11;
      } else {
        maxCentiCell = maxCentiCell + 9;
      }
    }

    //if(maxCentiCell==minCentiCell){
    return new Integer[] {cellId, minCentiCell};
    //}
    //}
    //return new Integer[]{cellId};
  }

  private static boolean isBoundingBoxCentiCell(double minLongitude, double minLatitude, double maxLongitude,
    double maxLatitude) {
    double width = maxLongitude > minLongitude ? maxLongitude - minLongitude : minLongitude - maxLongitude;
    double height = maxLatitude > minLatitude ? maxLatitude - minLatitude : minLatitude - maxLatitude;
    return Precision.round(height, 1) == 0.1f && Precision.round(width, 1) == 0.1f;
  }

  /**
   * For ease of conversion
   *
   * @param args See usage
   */
  public static void main(String[] args) {
    try {
      if (args.length == 1) {
        LatLngBoundingBox llbb = toBoundingBox(Integer.parseInt(args[0]));
        System.out.println(
          "CellId " + args[0] + ": minX[" + llbb.getMinLong() + "] minY[" + llbb.getMinLat() + "] maxX[" + llbb
            .getMaxLong() + "] maxY[" + llbb.getMaxLat() + "]");
      } else if (args.length == 2) {
        double lat = Double.parseDouble(args[0]);
        double lon = Double.parseDouble(args[1]);
        System.out.println("lat[" + lat + "] long[" + lon + "] = cellId: " + toCellId(lat, lon));
      } else {
        System.out.println("Provide either a 'cell id' or 'Lat Long' params!");
      }
    } catch (NumberFormatException | UnableToGenerateCellIdException e) {
      LOGGER.error("Error converting bounding box", e);
    }
  }
}
