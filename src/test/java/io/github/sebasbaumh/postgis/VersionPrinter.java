/*
 * VersionPrinter.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 *
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
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
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package io.github.sebasbaumh.postgis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;
import org.postgresql.Driver;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints out as much version information as available.
 */
@SuppressWarnings("javadoc")
public class VersionPrinter extends DatabaseTest
{

	private static final Logger logger = LoggerFactory.getLogger(VersionPrinter.class);

	private static String[] POSTGIS_FUNCTIONS = { "postgis_version", "postgis_proj_version",
			"postgis_scripts_installed", "postgis_lib_version", "postgis_scripts_released", "postgis_uses_stats",
			"postgis_geos_version", "postgis_scripts_build_date", "postgis_lib_build_date", "postgis_full_version",
			"postgis_gdal_version", "postgis_libjson_version", "postgis_libxml_version", "postgis_raster_lib_version",
			"postgis_svn_version" };

	private Connection connection = null;
	private Statement statement = null;

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

	private String getVersionString(String function) throws SQLException
	{
		String result = "-- unavailable -- ";
		try
		{
			try (ResultSet resultSet = statement.executeQuery("SELECT " + function + "()"))
			{
				if (resultSet.next())
				{
					String version = resultSet.getString(1);
					if (version != null)
					{
						result = version.trim();
					}
					else
					{
						result = "-- null result --";
					}
				}
				else
				{
					result = "-- no result --";
				}
			}
		}
		catch (SQLException sqle)
		{
			// If the function does not exist, a SQLException will be thrown, but it should be caught an swallowed if
			// the "does not exist" string is in the error message. The SQLException might be thrown for some other
			// problem not related to the missing function, so rethrow it if it doesn't contain the string.
			if (!PSQLState.UNDEFINED_FUNCTION.getState().equals(sqle.getSQLState()))
			{
				throw sqle;
			}
		}
		return result;
	}

	@Test
	public void test() throws Exception
	{
		if (!hasDatabase())
		{
			return;
		}

		// Print PostGIS version
		logger.info("*** PostGIS jdbc client code ***");
		// Print PostgreSQL JDBC Versions
		logger.info("*** PostgreSQL JDBC Driver ***");
		@SuppressWarnings("deprecation")
		String driverVersion = Driver.getVersion();
		Assert.assertNotNull(driverVersion);
		logger.info("\t getVersion: {}", driverVersion);

		try
		{
			Driver driver = new Driver();
			int majorVersion = driver.getMajorVersion();
			Assert.assertNotEquals(majorVersion, 0);
			logger.info("\t getMajorVersion: {}", majorVersion);
			int minorVersion = driver.getMinorVersion();
			Assert.assertNotEquals(minorVersion, 0);
			logger.info("\t getMinorVersion: {}", majorVersion);
		}
		catch (Exception e)
		{
			logger.error("Cannot create Driver instance: {}", e.getMessage());
		}

		// Print PostgreSQL server versions
		Assert.assertNotNull(connection);
		try (Statement statement = connection.createStatement())
		{
			logger.info("*** PostgreSQL Server ***");
			String versionString = getVersionString("version");
			logger.info("\t version: {}", versionString);

			// Print PostGIS versions
			logger.info("*** PostGIS Server ***");
			for (String GISVERSION : POSTGIS_FUNCTIONS)
			{
				versionString = getVersionString(GISVERSION);
				logger.info("\t {} version: {}", GISVERSION, versionString);
			}
		}
	}

}