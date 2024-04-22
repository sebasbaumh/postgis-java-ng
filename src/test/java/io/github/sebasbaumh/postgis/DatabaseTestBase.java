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

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.eclipse.jdt.annotation.Owning;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;

/**
 * A base class for tests relying on a database connection to a PostGIS database.
 * @author Sebastian Baumhekel
 */
public abstract class DatabaseTestBase
{
	/**
	 * Flag for first run.
	 */
	private static final AtomicBoolean bFirstRunFlag = new AtomicBoolean();
	private static final String CONFIG_JDBC_PASSWORD = "testJdbcPassword";
	/**
	 * Property for the JDBC URL to be used.
	 */
	public static final String CONFIG_JDBC_URL = "testJdbcUrl";
	private static final String CONFIG_JDBC_USERNAME = "testJdbcUsername";
	/**
	 * Name of the JDBC driver.
	 */
	private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

	private DataSource ds;
	private String jdbcPassword;
	private String jdbcUrl;
	private String jdbcUsername;

	/**
	 * Closes the given datasource.
	 * @param ds datasource
	 * @throws Exception
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("JVR_JDBC_VENDOR_RELIANCE")
	protected static void closeDataSource(DataSource ds) throws Exception
	{
		if (ds instanceof PooledDataSource)
		{
			DataSources.destroy(ds);
		}
		else if (ds instanceof AutoCloseable)
		{
			((AutoCloseable) ds).close();
		}
	}

	/**
	 * Will be called after the database has been setup.
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	protected void afterDatabaseSetup() throws SQLException
	{
	}

	/**
	 * Will be called before the database will be shutdown.
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	protected void beforeDatabaseShutdown() throws SQLException
	{
	}

	/**
	 * Gets a {@link Connection} to the database.
	 * @return {@link Connection}
	 * @throws SQLException
	 */
	@Owning
	protected Connection getConnection() throws SQLException
	{
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, ds);
		return ds.getConnection();
	}

	/**
	 * Converts the given WKT string to a {@link Geometry} of geography type.
	 * @param wkt WKT
	 * @return {@link Geometry}
	 * @throws SQLException
	 */
	protected Geometry getGeographyGeometryFromWKT(String wkt) throws SQLException
	{
		try (Connection conn = getConnection())
		{
			try (PreparedStatement pst = conn.prepareStatement("SELECT st_geogfromtext(?)"))
			{
				pst.setString(1, wkt);
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						Object o = rs.getObject(1);
						if (o instanceof PGgeography)
						{
							return ((PGgeography) o).getGeometry();
						}
					}
				}
			}
		}
		throw new IllegalArgumentException("could not get geometry for wkt: " + wkt);
	}

	/**
	 * Converts the given WKT string to WKB using a geometry of geography type.
	 * @param wkt WKT
	 * @return WKB on success, else null
	 * @throws SQLException
	 */
	protected String getGeographyWKBFromWKT(String wkt) throws SQLException
	{
		try (Connection conn = getConnection())
		{
			try (PreparedStatement pst = conn.prepareStatement("SELECT st_geogfromtext(?)"))
			{
				pst.setString(1, wkt);
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						return rs.getString(1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Converts the given WKT string to a {@link Geometry}.
	 * @param wkt WKT
	 * @return {@link Geometry}
	 * @throws SQLException
	 */
	protected Geometry getGeometryFromWKT(String wkt) throws SQLException
	{
		try (Connection conn = getConnection())
		{
			try (PreparedStatement pst = conn.prepareStatement("SELECT st_geomfromewkt(?)"))
			{
				pst.setString(1, wkt);
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						Object o = rs.getObject(1);
						if (o instanceof PGgeometry)
						{
							return ((PGgeometry) o).getGeometry();
						}
					}
				}
			}
		}
		throw new IllegalArgumentException("could not get geometry for wkt: " + wkt);
	}

	/**
	 * Builds a pooled {@link DataSource} with the given properties.
	 * @return {@link DataSource}
	 * @throws SQLException
	 */
	@Owning
	protected DataSource getPooledDataSource() throws SQLException
	{
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcUrl);
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcUsername);
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcPassword);
		try
		{
			ComboPooledDataSource pds = new ComboPooledDataSource();
			// initialize the pooled connection
			pds.setDriverClass(DRIVER_CLASS_NAME);
			pds.setJdbcUrl(jdbcUrl);
			pds.setUser(jdbcUsername);
			pds.setPassword(jdbcPassword);
			// set a timeout for checking out connections (30s)
			pds.setCheckoutTimeout(30000);
			return pds;
		}
		catch (PropertyVetoException e)
		{
			throw new SQLException(e);
		}
	}

	/**
	 * Builds an unpooled {@link DataSource} with the given properties.
	 * @return {@link DataSource}
	 * @throws SQLException
	 */
	protected DataSource getUnpooledDataSource() throws SQLException
	{
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcUrl);
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcUsername);
		Assert.assertNotNull("the following properties need to be configured for using a connection: " + CONFIG_JDBC_URL
				+ ", " + CONFIG_JDBC_USERNAME + ", " + CONFIG_JDBC_PASSWORD, jdbcPassword);
		return DataSources.unpooledDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
	}

	/**
	 * Converts the given WKT string to WKB.
	 * @param wkt WKT
	 * @return WKB on success, else null
	 * @throws SQLException
	 */
	protected String getWKBFromWKT(String wkt) throws SQLException
	{
		try (Connection conn = getConnection())
		{
			try (PreparedStatement pst = conn.prepareStatement("SELECT st_geomfromewkt(?)"))
			{
				pst.setString(1, wkt);
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						return rs.getString(1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Converts the given {@link Geometry} to a WKT string.
	 * @param geom {@link Geometry}
	 * @return WKT string
	 * @throws SQLException
	 */
	protected String getWKTFromGeometry(Geometry geom) throws SQLException
	{
		PGgeometry pgeom = new PGgeometry(geom);
		try (Connection conn = getConnection())
		{
			try (PreparedStatement pst = conn.prepareStatement("SELECT st_asewkt(?)"))
			{
				pst.setObject(1, pgeom);
				try (ResultSet rs = pst.executeQuery())
				{
					if (rs.next())
					{
						return rs.getString(1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks, if a database is available.
	 * @return true on success, else false
	 */
	protected boolean hasDatabase()
	{
		return ds != null;
	}

	/**
	 * Checks, if database credentials are available.
	 * @return true on success, else false
	 */
	protected boolean hasDatabaseCredentials()
	{
		return ((jdbcUrl != null) && (jdbcUsername != null) && (jdbcPassword != null));
	}

	/**
	 * Initializes the database.
	 * @throws SQLException
	 */
	@SuppressWarnings("deprecation")
	@Before
	public void initializeDatabase() throws SQLException
	{
		boolean bFirstRun = !bFirstRunFlag.getAndSet(true);
		try
		{
			// load connection details
			jdbcUrl = System.getProperty(CONFIG_JDBC_URL);
			jdbcUsername = System.getProperty(CONFIG_JDBC_USERNAME);
			jdbcPassword = System.getProperty(CONFIG_JDBC_PASSWORD);
			if ((jdbcUrl != null) && (jdbcUsername != null) && (jdbcPassword != null))
			{
				// load driver
				try
				{
					Class.forName(DRIVER_CLASS_NAME);
				}
				catch (ClassNotFoundException e)
				{
					throw new SQLException(e);
				}

				// disable C3p0 log spamming
				// the function is still working and the only workaround would be to set properties on the log4j logger,
				// but
				// it could also be another type if log4j is not available...
				MLog.getLogger("com.mchange.v2").setLevel(MLevel.WARNING);
				System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
				System.setProperty("com.mchange.v2.log.MLog", "log4j");

				// construct datasource
				ds = getUnpooledDataSource();
				Assert.assertNotNull(ds);
				if (bFirstRun)
				{
					System.out.println("Tests are running with a database");
				}
				afterDatabaseSetup();
			}
			else
			{
				if (bFirstRun)
				{
					System.out.println("Tests are running without a database");
				}
			}
		}
		catch (Exception ex)
		{
			System.err.println("Error setting up database for tests: " + ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Shuts down the underlying datasource.
	 * @throws Exception
	 */
	@After
	public void shutdownDatabase() throws Exception
	{
		beforeDatabaseShutdown();
		closeDataSource(ds);
	}

}
