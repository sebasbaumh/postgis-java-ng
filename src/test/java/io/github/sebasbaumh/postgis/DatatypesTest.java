/*
 * PostGIS extension for PostgreSQL JDBC driver
 *
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 * (C) 2018-2023 Sebastian Baumhekel, sebastian.baumhekel@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.sebasbaumh.postgis;

import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("javadoc")
public class DatatypesTest extends DatabaseTestBase
{

	private static final String CR_STR = "CIRCULARSTRING(-9 2,-8 3,-7 2)";

	private static final String CR_STR2 = "CIRCULARSTRING(0 -1,-1 0,0 1,1 0,0 -1)";

	private static final String LNG_STR = "LINESTRING  (10 10 20,20 20 20, 50 50 50, 34 34 34)";

	private static final Logger logger = LoggerFactory.getLogger(DatatypesTest.class);

	private static final String MLNG_STR = "MULTILINESTRING ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String MPLG_STR = "MULTIPOLYGON (((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))";

	private static final String PLG_STR = "POLYGON ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String PTG_STR = "POINT(10 10 20)";

	@SuppressWarnings("unchecked")
	private <T extends Geometry> T assertGeometry(Class<T> clazz, String wkt) throws SQLException
	{
		Geometry geom = getGeometryFromWKT(wkt);
		if (clazz.isInstance(geom))
		{
			return (T) geom;
		}
		throw new IllegalArgumentException(
				"expected: " + clazz.getCanonicalName() + " got: " + geom.getClass().getCanonicalName());
	}

	@Test
	public void testCircularString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testCircularString()");
		logger.debug(CR_STR);
		CircularString lng = assertGeometry(CircularString.class, CR_STR);
		logger.debug(lng.toString());
	}

	@Test
	public void testCircularString2() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testCircularString2()");
		logger.debug(CR_STR2);
		CircularString lng = assertGeometry(CircularString.class, CR_STR2);
		logger.debug(lng.toString());
	}

	@Test
	public void testLineString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testLineString()");
		logger.debug(LNG_STR);
		LineString lng = assertGeometry(LineString.class, LNG_STR);
		logger.debug(lng.toString());
	}

	@Test
	public void testMultiLineString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testMultiLineString()");
		logger.debug(MLNG_STR);
		MultiLineString mlng = assertGeometry(MultiLineString.class, MLNG_STR);
		logger.debug(mlng.toString());
	}

	@Test
	public void testMultiPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testMultiPolygon()");
		logger.debug(MPLG_STR);
		MultiPolygon mplg = assertGeometry(MultiPolygon.class, MPLG_STR);
		logger.debug(mplg.toString());
	}

	@Test
	public void testPGgeometry() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testPGgeometry()");
		logger.debug(MLNG_STR);
		PGgeometry pgf = new PGgeometry(getWKBFromWKT(MLNG_STR));
		logger.debug(pgf.toString());
	}

	@Test
	public void testPoint() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testPoint()");
		logger.debug(PTG_STR);
		Point ptg = assertGeometry(Point.class, PTG_STR);
		logger.debug(ptg.toString());
	}

	@Test
	public void testPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		logger.trace("void testPolygon()");
		logger.debug(PLG_STR);
		Polygon plg = assertGeometry(Polygon.class, PLG_STR);
		logger.debug(plg.toString());
	}

}