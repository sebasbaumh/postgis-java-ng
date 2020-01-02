package io.github.sebasbaumh.postgis;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test geometries of geography type.
 * @author Sebastian Baumhekel
 */
@SuppressWarnings("javadoc")
public class GeographyTest extends PostgisDatabaseTest
{
	private static final String lng_str = "LINESTRING  (10 10 20,20 20 20, 50 50 50, 34 34 34)";

	private static final String mlng_str = "MULTILINESTRING ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String mplg_str = "MULTIPOLYGON (((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))";

	private static final String plg_str = "POLYGON ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

	private static final String ptg_str = "POINT(10 10 20)";

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
		assertGeometry(LineString.class, lng_str);
	}

	@Test
	public void testMultiLineString() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(MultiLineString.class, mlng_str);
	}

	@Test
	public void testMultiPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(MultiPolygon.class, mplg_str);
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
		new PGgeometry(getWKBFromWKT(mlng_str));
	}

	@Test
	public void testPoint() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(Point.class, ptg_str);
	}

	@Test
	public void testPolygon() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		assertGeometry(Polygon.class, plg_str);
	}

}
