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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.sebasbaumh.postgis.binary.BinaryParser;
import io.github.sebasbaumh.postgis.binary.BinaryWriter;

@SuppressWarnings("javadoc")
public class ParserTest extends DatabaseTestBase
{
	private static final Logger logger = LoggerFactory.getLogger(ParserTest.class);
	/** The srid we use for the srid tests */
	private static final int SRID = 4326;
	/** The string prefix we get for the srid tests */
	private static final String SRIDPREFIX = "SRID=" + SRID + ";";
	private Connection connection = null;
	private Statement statement = null;

	/** Pass a EWKB geometry representation through the server */
	private static Geometry binaryViaSQL(byte[] rep, Connection conn) throws SQLException
	{
		try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT ?::bytea::geometry"))
		{
			preparedStatement.setBytes(1, rep);
			try (ResultSet resultSet = preparedStatement.executeQuery())
			{
				resultSet.next();
				PGgeometry resultwrapper = ((PGgeometry) resultSet.getObject(1));
				return resultwrapper.getGeometry();
			}
		}
	}

	/** Pass a geometry representation through the SQL server via EWKB */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	private static Geometry ewkbViaSQL(String rep, Statement stat) throws SQLException
	{
		try (ResultSet resultSet = stat.executeQuery("SELECT ST_AsEWKB(geometry_in('" + rep + "'))"))
		{
			resultSet.next();
			byte[] resrep = resultSet.getBytes(1);
			return BinaryParser.parse(PostGisUtil.toHexString(resrep));
		}
	}

	/**
	 * Tests, if the given {@link Geometry} supports a binary transfer.
	 * @param geom
	 * @throws SQLException
	 */
	private static void testBinaryObject(Geometry geom) throws SQLException
	{
		// create a PGgeometry to be able to get the binary form.
		PGgeometry pgeom = new PGgeometry(geom);
		int length = pgeom.lengthInBytes();
		Assert.assertTrue("length is 0!", length > 0);
		// get the binary data
		byte[] data = new byte[length];
		pgeom.toBytes(data, 0);
		// create a new PGgeometry
		PGgeometry pgeom2 = new PGgeometry();
		// set the byte data
		pgeom2.setByteValue(data, 0);
		// get the geometry and make sure it equals the given one
		Geometry geom2 = pgeom2.getGeometry();
		Assert.assertEquals(geom, geom2);
	}

	/**
	 * Pass a geometry representation through the SQL server via prepared statement
	 */
	private static Geometry viaPrepSQL(Geometry geom, Connection conn) throws SQLException
	{
		try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT ?::geometry"))
		{
			PGgeometry wrapper = new PGgeometry(geom);
			preparedStatement.setObject(1, wrapper, Types.OTHER);
			try (ResultSet resultSet = preparedStatement.executeQuery())
			{
				resultSet.next();
				PGgeometry resultwrapper = (PGgeometry) resultSet.getObject(1);
				return resultwrapper.getGeometry();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.DatabaseTest#afterDatabaseSetup()
	 */
	@Override
	protected void afterDatabaseSetup() throws SQLException
	{
		connection = getConnection();
		statement = connection.createStatement();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.DatabaseTest#beforeDatabaseShutdown()
	 */
	@Override
	protected void beforeDatabaseShutdown() throws SQLException
	{
		if ((statement != null) && (!statement.isClosed()))
		{
			statement.close();
		}
		if ((connection != null) && (!connection.isClosed()))
		{
			connection.close();
		}
	}

	/** Pass a geometry representation through the SQL server via EWKT */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	private Geometry ewktViaSQL(String rep, Statement stat) throws SQLException
	{
		try (ResultSet resultSet = stat.executeQuery("SELECT ST_AsEWKT(geometry_in('" + rep + "'))"))
		{
			resultSet.next();
			String resrep = resultSet.getString(1);
			return getGeometryFromWKT(resrep);
		}
	}

	private void test(String WKT, int srid) throws SQLException
	{
		// first get the geometry from the WKT string
		logger.debug("Original: {} ", WKT);
		Geometry geom = getGeometryFromWKT(WKT);
		Assert.assertEquals("SRIDs are not equal", srid, geom.getSrid());

		// check binary object (without actually requiring binary transfer to the database)
		testBinaryObject(geom);

		// turn geometry into WKT
		String parsed = getWKTFromGeometry(geom);
		logger.debug("Parsed: {}", parsed);
		// and parse that again
		Geometry regeom = getGeometryFromWKT(parsed);
		// get WKT of that reparsed geometry (do not compare to given WKT as that might not match the WKT build by the
		// parsing function)
		String reparsed = getWKTFromGeometry(regeom);
		logger.debug("Re-Parsed: {}", reparsed);
		// make sure geometries and WKT are equal
		Assert.assertEquals("Geometries are not equal", geom, regeom);
		Assert.assertEquals("Text Reps are not equal", reparsed, parsed);

		String hexNWKT = BinaryWriter.writeHexed(regeom);
		logger.debug("NDRHex: {}", hexNWKT);
		regeom = getGeometryFromWKT(hexNWKT);
		logger.debug("ReNDRHex: {}", regeom);
		Assert.assertEquals("Geometries are not equal", geom, regeom);

		byte[] NWKT = PostGisUtil.toHexBytes(BinaryWriter.writeHexed(regeom));
		regeom = BinaryParser.parse(PostGisUtil.toHexString(NWKT));
		logger.debug("NDR: {}", regeom);
		Assert.assertEquals("Geometries are not equal", geom, regeom);

		logger.debug("Testing on connection {}", connection.getCatalog());

		Geometry sqlGeom = viaSQL(WKT);
		logger.debug("SQLin: {}", sqlGeom);
		if (!geom.equals(sqlGeom))
		{
			logger.warn("Geometries after SQL are not equal");
			Assert.fail();
		}

		Geometry sqlreGeom = viaSQL(parsed);
		logger.debug("SQLout: {}", sqlreGeom);
		if (!geom.equals(sqlreGeom))
		{
			logger.warn("Reparsed Geometries after SQL are not equal!");
			Assert.fail();
		}

		sqlreGeom = viaPrepSQL(geom, connection);
		logger.debug("Prepared: {}", getWKTFromGeometry(sqlreGeom));
		if (!geom.equals(sqlreGeom))
		{
			logger.warn("Reparsed Geometries after prepared StatementSQL are not equal!");
			Assert.fail();
		}

		// asEWKT() function is not present on PostGIS 0.X, and the test
		// is pointless as 0.X uses EWKT as canonical rep so the same
		// functionality was already tested above.
		sqlGeom = ewktViaSQL(WKT, statement);
		logger.debug("asEWKT: {}", sqlGeom);
		Assert.assertEquals(geom, sqlGeom);

		// asEWKB() function is not present on PostGIS 0.X.
		sqlGeom = ewkbViaSQL(WKT, statement);
		logger.debug("asEWKB: {}", sqlGeom);
		Assert.assertEquals(geom, sqlGeom);

		// HexEWKB parsing is not present on PostGIS 0.X.
		sqlGeom = viaSQL(hexNWKT);
		logger.debug("hexNWKT: {}", sqlGeom);
		Assert.assertEquals(geom, sqlGeom);

		// Canonical binary input is not present before 1.0
		sqlGeom = binaryViaSQL(NWKT, connection);
		logger.debug("NWKT: {}", sqlGeom);
		Assert.assertEquals(geom, sqlGeom);
	}

	/**
	 * Tests with and without SRID prefix (using database).
	 * @param wkt WKT
	 * @throws SQLException
	 */
	private void testAll(String wkt) throws SQLException
	{
		test(wkt, Geometry.UNKNOWN_SRID);
		test(SRIDPREFIX + wkt, SRID);
	}

	/**
	 * Test using a PostGIS database, will do nothing if no database is configured.
	 * @throws Exception
	 */
	@Test
	public void testParserUsingDb() throws Exception
	{
		if (!hasDatabase())
		{
			return;
		}
		// 2D
		testAll("POINT(10 10)");
		// 3D with 3rd coordinate set to 0
		testAll("POINT(10 10 0)");
		// 3D
		testAll("POINT(10 10 20)");
		// 3D with scientific notation
		// NOTE: the original e-100 is a corner case in terms of precision on the latest PostGIS version (2.5.3), so we
		// use a smaller exponent)
		// testAll("POINT(1e100 1.2345e-100 -2e-5)");
		//@formatter:off
		/* to proof just try that SQL in PostGIS:
		 SELECT st_asewkt(st_geomfromewkt('POINT(1e100 1e-12 -2e-5)'));
		 --------------------> results in 'POINT(1e+100 0 -0.00002)'
		 */
		//@formatter:on
		testAll("POINT(1e100 1.2345e-11 -2e-5)");
		// 2D + Measures
		testAll("POINTM(10 10 20)");
		// 3D + Measures
		testAll("POINT(10 10 20 30)");
		// broken format, see http://lists.jump-project.org/pipermail/jts-devel/2006-April/001572.html
		testAll("MULTIPOINT(11 12, 20 20)");
		// broken format
		testAll("MULTIPOINT(11 12 13, 20 20 20)");
		// broken format
		testAll("MULTIPOINTM(11 12 13, 20 20 20)");
		// broken format
		testAll("MULTIPOINT(11 12 13 14,20 20 20 20)");
		// OGC conforming format
		testAll("MULTIPOINT((11 12), (20 20))");
		testAll("MULTIPOINT((11 12 13), (20 20 20))");
		testAll("MULTIPOINTM((11 12 13), (20 20 20))");
		testAll("MULTIPOINT((11 12 13 14),(20 20 20 20))");
		testAll("LINESTRING(10 10,20 20,50 50,34 34)");
		testAll("LINESTRING(10 10 20,20 20 20,50 50 50,34 34 34)");
		testAll("LINESTRINGM(10 10 20,20 20 20,50 50 50,34 34 34)");
		testAll("LINESTRING(10 10 20 20,20 20 20 20,50 50 50 50,34 34 34 50)");
		testAll("POLYGON((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))");
		testAll("POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))");
		testAll("POLYGONM((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))");
		testAll("POLYGON((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))");
		testAll("MULTIPOLYGON(((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)),((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)))");
		testAll("MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		testAll("MULTIPOLYGONM(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		testAll("MULTIPOLYGON(((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)),((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)))");
		testAll("MULTILINESTRING((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))");
		testAll("MULTILINESTRING((10 10 5,20 10 5,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))");
		testAll("MULTILINESTRINGM((10 10 7,20 10 7,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))");
		testAll("MULTILINESTRING((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))");
		testAll("GEOMETRYCOLLECTION(POINT(10 10),POINT(20 20))");
		testAll("GEOMETRYCOLLECTION(POINT(10 10 20),POINT(20 20 20))");
		testAll("GEOMETRYCOLLECTION(POINT(10 10 20 7),POINT(20 20 20 7))");
		testAll("GEOMETRYCOLLECTION(LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34))");
		testAll("GEOMETRYCOLLECTION(POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		// Cannot be parsed by 0.X servers, broken format
		testAll("GEOMETRYCOLLECTION(MULTIPOINT(10 10 10, 20 20 20),MULTIPOINT(10 10 10, 20 20 20))");
		// Cannot be parsed by 0.X servers, OGC conformant
		testAll("GEOMETRYCOLLECTION(MULTIPOINT((10 10 10), (20 20 20)),MULTIPOINT((10 10 10), (20 20 20)))");
		// PostGIs 0.X "flattens" this geometry, so it is not
		// equal after reparsing.
		testAll("GEOMETRYCOLLECTION(MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		// PostGIs 0.X "flattens" this geometry, so it is not equal
		// after reparsing.
		testAll("GEOMETRYCOLLECTION(MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))))");
		testAll("GEOMETRYCOLLECTION(POINT(10 10 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		// Collections that contain both X and MultiX do not work on
		// PostGIS 0.x, broken format
		testAll("GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT(10 10 10, 20 20 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		// Collections that contain both X and MultiX do not work on
		// PostGIS 0.x, OGC conformant
		testAll("GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT((10 10 10), (20 20 20)),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))");
		// new (correct) representation
		testAll("GEOMETRYCOLLECTION EMPTY");
		testAll("GEOMETRYCOLLECTIONM(POINTM(10 10 20),POINTM(20 20 20))");
		testAll("CIRCULARSTRING(-9 2,-8 3,-7 2)");
		testAll("CIRCULARSTRING(0 -1,-1 0,0 1,1 0,0 -1)");
		testAll("CURVEPOLYGON((0 0, 4 0, 4 4, 0 4, 0 0),(1 1, 3 3, 3 1, 1 1))");
		testAll("CURVEPOLYGON(CIRCULARSTRING(0 0, 4 0, 4 4, 0 4, 0 0),(1 1, 3 3, 3 1, 1 1))");
		testAll("COMPOUNDCURVE(CIRCULARSTRING(0 0, 1 1, 1 0),(1 0, 0 1))");
		testAll("MULTICURVE((0 0, 5 5),CIRCULARSTRING(4 0, 4 4, 8 4))");
		testAll("MULTISURFACE(CURVEPOLYGON(CIRCULARSTRING(0 0, 4 0, 4 4, 0 4, 0 0),(1 1, 3 3, 3 1, 1 1)),((10 10, 14 12, 11 10, 10 10),(11 11, 11.5 11, 11 11.5, 11 11)))");
		// end
	}

	/** Pass a geometry representation through the SQL server */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	private Geometry viaSQL(String rep) throws SQLException
	{
		logger.trace("Geometry viaSQL(String rep)");
		logger.trace("[P] rep => {}", rep);
		try (ResultSet resultSet = statement.executeQuery("SELECT geometry_in('" + rep + "')"))
		{
			resultSet.next();
			return ((PGgeometry) resultSet.getObject(1)).getGeometry();
		}
	}

}