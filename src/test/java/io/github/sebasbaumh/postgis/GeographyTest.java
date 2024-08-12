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

/**
 * Test geometries of geography type.
 * @author Sebastian Baumhekel
 */
@SuppressWarnings("javadoc")
public class GeographyTest extends PostgisDatabaseTest
{
	private static final String LNG_STR = "LINESTRING  (10 10 20,20 20 20, 50 50 50, 34 34 34)";

	private static final String MLNG_STR = "MULTILINESTRING ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String MPLG_STR = "MULTIPOLYGON (((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))";

	private static final String PLG_STR = "POLYGON ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String PTG_STR = "POINT(10 10 20)";

	@SuppressWarnings("unchecked")
	private <T extends Geometry> T assertGeometry(Class<T> clazz, String wkt) throws SQLException
	{
		Geometry geom = getGeographyGeometryFromWKT(wkt);
		if (clazz.isInstance(geom))
		{
			return (T) geom;
		}
		throw new IllegalArgumentException(
				"expected: " + clazz.getCanonicalName() + " got: " + geom.getClass().getCanonicalName());
	}

	@Test
	public void testLineString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(LineString.class, LNG_STR);
	}

	@Test
	public void testMultiLineString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(MultiLineString.class, MLNG_STR);
	}

	@Test
	public void testMultiPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(MultiPolygon.class, MPLG_STR);
	}

	@SuppressWarnings("unused")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
	@Test
	public void testPGgeometry() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		new PGgeometry(getWKBFromWKT(MLNG_STR));
	}

	@Test
	public void testPoint() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(Point.class, PTG_STR);
	}

	@Test
	public void testPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(Polygon.class, PLG_STR);
	}

}
