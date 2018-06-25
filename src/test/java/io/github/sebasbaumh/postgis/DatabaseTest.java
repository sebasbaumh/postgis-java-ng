package io.github.sebasbaumh.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;

/**
 * A base class for tests relying on a database connection to a PostGIS database.
 * @author Sebastian Baumhekel
 */
public abstract class DatabaseTest
{
	private static final String CONFIG_JDBC_PASSWORD = "testJdbcPassword";
	private static final String CONFIG_JDBC_URL = "testJdbcUrl";
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
	protected Connection getConnection() throws SQLException
	{
		Assert.assertNotNull("the following property need to be configured for using a connection: "+CONFIG_JDBC_URL,ds);
		return ds.getConnection();
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
		return ds!=null;
	}

	/**
	 * Initializes the database.
	 * @throws SQLException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("FCBL_FIELD_COULD_BE_LOCAL")
	@SuppressWarnings("deprecation")
	@Before
	public void initializeDatabase() throws SQLException
	{
		// load connection details
		jdbcUrl = System.getProperty(CONFIG_JDBC_URL);
		if(jdbcUrl!=null)
		{
			jdbcUsername = System.getProperty(CONFIG_JDBC_USERNAME);
			jdbcPassword = System.getProperty(CONFIG_JDBC_PASSWORD);
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
			// the function is still working and the only workaround would be to set properties on the log4j logger, but it
			// could also be another type if log4j is not available...
			MLog.getLogger("com.mchange.v2").setLevel(MLevel.WARNING);
			System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
			System.setProperty("com.mchange.v2.log.MLog", "log4j");
	
			// construct datasource
			if ((jdbcUsername != null) && !jdbcUsername.trim().isEmpty())
			{
				ds = DataSources.unpooledDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
			}
			else
			{
				ds = DataSources.unpooledDataSource(jdbcUrl);
			}
			Assert.assertNotNull(ds);
			afterDatabaseSetup();
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
		if (ds instanceof AutoCloseable)
		{
			((AutoCloseable) ds).close();
		}
	}

}