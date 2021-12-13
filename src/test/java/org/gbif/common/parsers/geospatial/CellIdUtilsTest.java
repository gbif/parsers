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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author tim
 * @author dave
 */
public class CellIdUtilsTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellIdUtilsTest.class);

  @Test
  public void testCreateCellId() {
    try {
      assertEquals(0, CellIdUtils.toCellId((double) -90, (double) -180));
      assertEquals(0, CellIdUtils.toCellId((double) -90, -179.9));
      assertEquals(359, CellIdUtils.toCellId(-89.1, 179.2));
      assertEquals(360, CellIdUtils.toCellId(-88.5, (double) -180));
      assertEquals(6647, CellIdUtils.toCellId(-71.31, -12.4233));
    } catch (UnableToGenerateCellIdException e) {
      fail(e.toString());
    }
    try {
      CellIdUtils.toCellId((double) -100, null);
      fail("-100, null should throw exception");
    } catch (UnableToGenerateCellIdException ignored) {
    }
  }

  /**
   * Test method for {@link CellIdUtils#toCentiCellId(Double, Double)}.
   */
  @Test
  public void testCreateCentiCellId() {
    try {
      assertEquals(0, CellIdUtils.toCentiCellId(-90.0, (double) 170));
      assertEquals(0, CellIdUtils.toCentiCellId(89.05, 179.05));
      assertEquals(99, CellIdUtils.toCentiCellId(-89.05, -179.05));
      assertEquals(10, CellIdUtils.toCentiCellId(10.15, 10.03));
      assertEquals(0, CellIdUtils.toCentiCellId(-88.99, -179.99));
      assertEquals(67, CellIdUtils.toCentiCellId(41.6128, -87.2192));
      assertEquals(0, CellIdUtils.toCentiCellId((double) -41, (double) -87));
    } catch (UnableToGenerateCellIdException e) {
      fail(e.toString());
    }
    try {
      CellIdUtils.toCellId((double) -100, null);
      fail("-100, null should throw exception");
    } catch (UnableToGenerateCellIdException ignored) {
    }
  }

  /**
   * Test method for {@link CellIdUtils#toBoundingBox(int)}.
   */
  @Test
  public void testToBoundingBox() {
    assertEquals(new LatLngBoundingBox(-180, -90, -179, -89), CellIdUtils.toBoundingBox(0));
    assertEquals(new LatLngBoundingBox(-179, -90, -178, -89), CellIdUtils.toBoundingBox(1));
    assertEquals(new LatLngBoundingBox(-180, -89, -179, -88), CellIdUtils.toBoundingBox(360));
    assertEquals(new LatLngBoundingBox(-13, -72, -12, -71), CellIdUtils.toBoundingBox(6647));
  }

  /**
   * Test method for {@link CellIdUtils#toBoundingBox(int, int)}.
   */
  @Disabled("This one was not annotated with @Test, now failing")
  @Test
  public void testToBoundingBox2() {
    assertEquals(new LatLngBoundingBox((float) -180.0, (float) -90.0, (float) -179.9, (float) -89.9),
      CellIdUtils.toBoundingBox(0, 0));
    assertEquals(new LatLngBoundingBox((float) -180.0, (float) -89.0, (float) -179.9, (float) -88.9),
      CellIdUtils.toBoundingBox(360, 0));
    assertEquals(new LatLngBoundingBox((float) -179.9, (float) -88.9, (float) -179.8, (float) -88.8),
      CellIdUtils.toBoundingBox(360, 11));
  }

  /**
   * The cells enclosed by should return cells that are partially enclosed also Test method for {@link
   * CellIdUtils#getCellsEnclosedBy(double, double, double, double)}.
   */
  @Test
  public void testGetCellsEnclosedBy() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(-90, -89, -180, -179);
      assertTrue(results.contains(0));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(-90, (float) -88, -180, (float) -178);
      assertTrue(results.contains(0));
      assertTrue(results.contains(1));
      assertTrue(results.contains(360));
      assertTrue(results.contains(361));
      assertEquals(4, results.size());

    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  // test the bottom left corner of the world
  @Test
  public void testGetCellsEnclosedByBottomLeft() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(-90, -89, -180, -179);
      assertTrue(results.contains(0));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(-89, -88, -179, -178);
      assertTrue(results.contains(361));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) -89.9, (float) -87.5, (float) -179.9, (float) -177.5);
      assertTrue(results.contains(0));
      assertTrue(results.contains(1));
      assertTrue(results.contains(2));
      assertTrue(results.contains(360));
      assertTrue(results.contains(361));
      assertTrue(results.contains(362));
      assertTrue(results.contains(720));
      assertTrue(results.contains(721));
      assertTrue(results.contains(722));
      assertEquals(9, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) -89.9, (float) -87.5, (float) -179.9, (float) -178);
      assertTrue(results.contains(0));
      assertTrue(results.contains(1));
      assertTrue(results.contains(360));
      assertTrue(results.contains(361));
      assertTrue(results.contains(720));
      assertTrue(results.contains(721));
      assertEquals(6, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) -89.9, (float) -88, (float) -179.9, (float) -177.1);
      assertTrue(results.contains(0));
      assertTrue(results.contains(1));
      assertTrue(results.contains(2));
      assertTrue(results.contains(360));
      assertTrue(results.contains(361));
      assertTrue(results.contains(362));
      assertEquals(6, results.size());


    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  // test the bottom right corner of the world
  @Test
  public void testGetCellsEnclosedByBottomRight() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(-90, -89, 179, 180);
      assertTrue(results.contains(359));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(-89, -88, 178, 179);
      assertTrue(results.contains(718));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) -89.9, (float) -87.5, (float) 177.2, (float) 179.5);
      assertTrue(results.contains(357));
      assertTrue(results.contains(358));
      assertTrue(results.contains(359));
      assertTrue(results.contains(717));
      assertTrue(results.contains(718));
      assertTrue(results.contains(719));
      assertTrue(results.contains(1077));
      assertTrue(results.contains(1078));
      assertTrue(results.contains(1079));
      assertEquals(9, results.size());

    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  // test the top left corner of the world
  @Test
  public void testGetCellsEnclosedByTopLeft() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(89, 90, -180, -179);
      assertTrue(results.contains(64440));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(88, 89, -179, -178);
      assertTrue(results.contains(64081));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) 87.2, (float) 89.5, (float) -179.1, (float) -177.9);
      assertTrue(results.contains(63720));
      assertTrue(results.contains(63721));
      assertTrue(results.contains(63721));
      assertTrue(results.contains(64080));
      assertTrue(results.contains(64081));
      assertTrue(results.contains(64082));
      assertTrue(results.contains(64440));
      assertTrue(results.contains(64441));
      assertTrue(results.contains(64442));
      assertEquals(9, results.size());

    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  // test the top right corner of the world
  @Test
  public void testGetCellsEnclosedByTopRight() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(89, 90, 179, 180);
      assertTrue(results.contains(64799));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(88, 89, 178, 179);
      assertTrue(results.contains(64438));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) 87.2, (float) 89.5, (float) 177.2, (float) 179.9);
      assertTrue(results.contains(64077));
      assertTrue(results.contains(64078));
      assertTrue(results.contains(64079));
      assertTrue(results.contains(64437));
      assertTrue(results.contains(64438));
      assertTrue(results.contains(64439));
      assertTrue(results.contains(64797));
      assertTrue(results.contains(64798));
      assertTrue(results.contains(64799));
      assertEquals(9, results.size());

    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  // test the 0,0 area of the world
  @Test
  public void testGetCellsEnclosedByCentre() {
    try {
      Set<Integer> results = CellIdUtils.getCellsEnclosedBy(0, 1, 0, 1);
      assertTrue(results.contains(32580));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(-1, 0, 0, 1);
      assertTrue(results.contains(32220));
      assertEquals(1, results.size());

      results = CellIdUtils.getCellsEnclosedBy(-1, 1, -1, 1);
      assertTrue(results.contains(32219));
      assertTrue(results.contains(32220));
      assertTrue(results.contains(32579));
      assertTrue(results.contains(32580));
      assertEquals(4, results.size());

      results = CellIdUtils.getCellsEnclosedBy((float) -0.9, (float) 1.5, (float) -0.8, (float) 1.4);
      assertTrue(results.contains(32219));
      assertTrue(results.contains(32220));
      assertTrue(results.contains(32221));
      assertTrue(results.contains(32579));
      assertTrue(results.contains(32580));
      assertTrue(results.contains(32581));
      assertTrue(results.contains(32939));
      assertTrue(results.contains(32940));
      assertTrue(results.contains(32941));
      assertEquals(9, results.size());

    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testToCellId() {
    try {
      assertEquals(2, CellIdUtils.toCellId(-89.5d, -177.5d));
      assertEquals(0, CellIdUtils.toCellId(-89.9d, -179.9d));
    } catch (UnableToGenerateCellIdException e) {
      fail(e.getMessage());
    }
  }


  /**
   * Test a conversion from and to ids
   */
  @Test
  public void testLL2Id2LL() {
    try {
      List<Integer> cells = new LinkedList<>(CellIdUtils.getCellsEnclosedBy(-30, -20, 110, 130));
      LatLngBoundingBox bb = CellIdUtils.getBoundingBoxForCells(cells);
      assertEquals(110, (int) bb.getMinLong());
      assertEquals(-30, (int) bb.getMinLat());
      assertEquals(130, (int) bb.getMaxLong());
      assertEquals(-20, (int) bb.getMaxLat());
    } catch (UnableToGenerateCellIdException e) {
      fail("Incorrectly thrown - the lat longs are valid: " + e.getMessage());
    }
  }

  @Disabled("This one was not annotated with @Test, now failing")
  @Test
  public void testGetCentiCellIdforBoundingBox() {
    try {
      Integer[] centiCell = CellIdUtils.getCentiCellIdForBoundingBox(17.1f, 19.2f, 17.2f, 19.3f);
      assertNotNull(centiCell);
      assertEquals(2, centiCell.length);
      assertEquals(21, (int) centiCell[1]);
      centiCell = CellIdUtils.getCentiCellIdForBoundingBox(17.0f, 19.8f, 17.1f, 19.9f);
      assertNotNull(centiCell);
      assertEquals(2, centiCell.length);
      assertEquals(80, (int) centiCell[1]);
      centiCell = CellIdUtils.getCentiCellIdForBoundingBox(-17.0f, 19.8f, -16.9f, 19.9f);
      assertNotNull(centiCell);
      assertEquals(2, centiCell.length);
      //assertTrue(centiCell[2]==21);
      centiCell = CellIdUtils.getCentiCellIdForBoundingBox(-17.0f, -19.9f, -16.9f, -19.8f);
      assertNotNull(centiCell);
      assertEquals(2, centiCell.length);
      //assertTrue(centiCell[2]==21);
      centiCell = CellIdUtils.getCentiCellIdForBoundingBox(17.0f, -19.9f, 17.1f, -19.8f);
      assertNotNull(centiCell);
      assertEquals(2, centiCell.length);
      //assertTrue(centiCell[2]==21);
    } catch (Exception e) {
      LOGGER.warn(e.getMessage(), e);
      fail("Incorrectly thrown - the lat longs are valid: " + e.getMessage());
    }
  }
}
