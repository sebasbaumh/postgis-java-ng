/*
 * DriverWrapper.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - Wrapper utility class
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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.Driver;
import org.postgresql.PGConnection;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;
import org.postgresql.core.QueryExecutor;

/**
 * Wraps the PostGreSQL Driver to transparently add the PostGIS Object Classes. This avoids the need of explicit
 * addDataType() calls from the driver users side.
 * <p>
 * This method currently works with J2EE DataSource implementations, and with DriverManager framework.
 * <p>
 * Simply replace the "jdbc:postgresql:" with a "jdbc:postgresql_postGIS:" in the jdbc URL.
 * <p>
 * When using the drivermanager, you need to initialize {@link DriverWrapper} instead of (or in addition to)
 * {@link org.postgresql.Driver}. When using a J2EE DataSource implementation, set the driver class property in the
 * datasource config, the following works for jboss: <code>
 * &lt;driver-class&gt;io.github.sebasbaumh.postgis.DriverWrapper&lt;/driver-class&gt;
 * </code>
 * <p>
 * If you don't like or want to use the {@link DriverWrapper}, you can just call {@link #registerDataTypes(Connection)}
 * on your {@link Connection}.
 * <p>
 * This wrapper always uses EWKB as representation, and thus works against PostGIS servers starting from 2.3.
 * <p>
 * original author {@literal Markus Schaber <markus.schaber@logix-tt.com>}
 * <p>
 * reworked by Sebastian Baumhekel
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class DriverWrapper extends Driver
{
	private static final Logger logger = Logger.getLogger("io.github.sebasbaumh.postgis.DriverWrapper");
	/**
	 * PostGIS custom JDBC protocol.
	 */
	public static final String POSTGIS_PROTOCOL = "jdbc:postgresql_postGIS:";
	/**
	 * PostgreSQL JDBC protocol.
	 */
	public static final String POSTGRES_PROTOCOL = "jdbc:postgresql:";

	/**
	 * Static constructor registering the driver.
	 */
	static
	{
		try
		{
			// Try to register ourself to the DriverManager
			java.sql.DriverManager.registerDriver(new DriverWrapper());
		}
		catch (SQLException e)
		{
			logger.log(Level.WARNING, "Error registering PostGIS Wrapper Driver", e);
		}
	}

	/**
	 * Default constructor.
	 */
	public DriverWrapper()
	{
	}

	/**
	 * Tries to get a {@link QueryExecutor} from the given {@link Connection}, supports wrapped connections.
	 * @param conn {@link Connection}
	 * @return {@link QueryExecutor}
	 * @throws SQLException if the {@link Connection} is no {@link BaseConnection}.
	 */
	private static QueryExecutor getQueryExecutor(Connection conn) throws SQLException
	{
		// try to get underlying PGConnection
		PGConnection pgconn = tryUnwrap(conn);
		// if instance is found, add the geometry types to the connection
		if (pgconn instanceof BaseConnection)
		{
			return ((BaseConnection) pgconn).getQueryExecutor();
		}
		// try to unwrap connections coming from c3p0 connection pools
		try
		{
			Class<?> clazzC3P0ProxyConnection = Class.forName("com.mchange.v2.c3p0.C3P0ProxyConnection");
			if (clazzC3P0ProxyConnection.isInstance(conn))
			{
				// use method Object rawConnectionOperation(Method m, Object target, Object[] args)
				Method mrawConnectionOperation = clazzC3P0ProxyConnection.getMethod("rawConnectionOperation",
						Method.class, Object.class, Object[].class);
				Method mAddDataType = BaseConnection.class.getMethod("getQueryExecutor");
				return (QueryExecutor) mrawConnectionOperation.invoke(conn, mAddDataType, null, null);
			}
		}
		catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex)
		{
			// ignore all errors here
		}
		// BaseConnection could not be found
		throw new SQLException(
				"Connection is neither an org.postgresql.core.BaseConnection, nor a Connection wrapped around an org.postgresql.core.BaseConnection.");
	}

	/**
	 * Mangles the PostGIS URL to return the original PostGreSQL URL
	 * @param url String containing the url to be "mangled"
	 * @return "mangled" string on success, else null
	 */
	@Nullable
	private static String mangleURL(String url)
	{
		if (url.startsWith(POSTGIS_PROTOCOL))
		{
			return POSTGRES_PROTOCOL + url.substring(POSTGIS_PROTOCOL.length());
		}
		else
		{
			// unknown protocol or subprotocol in url
			return null;
		}
	}

	/**
	 * Registers all datatypes on the given connection, supports wrapped connections unlike
	 * {@link #registerDataTypes(PGConnection)}.
	 * @param conn {@link Connection}
	 * @throws SQLException if the {@link Connection} is neither an {@link org.postgresql.PGConnection}, nor a
	 *             {@link Connection} wrapped around an {@link org.postgresql.PGConnection}.
	 */
	public static void registerDataTypes(Connection conn) throws SQLException
	{
		// try to get underlying PGConnection
		PGConnection pgconn = tryUnwrap(conn);
		// if instance is found, add the geometry types to the connection
		if (pgconn != null)
		{
			registerDataTypes(pgconn);
			return;
		}
		// try to unwrap connections coming from c3p0 connection pools
		try
		{
			Class<?> clazzC3P0ProxyConnection = Class.forName("com.mchange.v2.c3p0.C3P0ProxyConnection");
			if (clazzC3P0ProxyConnection.isInstance(conn))
			{
				// use method Object rawConnectionOperation(Method m, Object target, Object[] args)
				Method mrawConnectionOperation = clazzC3P0ProxyConnection.getMethod("rawConnectionOperation",
						Method.class, Object.class, Object[].class);
				Method mAddDataType = PGConnection.class.getMethod("addDataType", String.class, Class.class);
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "geometry", io.github.sebasbaumh.postgis.PGgeometry.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "geography", io.github.sebasbaumh.postgis.PGgeography.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "box2d", io.github.sebasbaumh.postgis.PGbox2d.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "box3d", io.github.sebasbaumh.postgis.PGbox3d.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "public.geometry", io.github.sebasbaumh.postgis.PGgeometry.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "public.geography", io.github.sebasbaumh.postgis.PGgeography.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "public.box2d", io.github.sebasbaumh.postgis.PGbox2d.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "public.box3d", io.github.sebasbaumh.postgis.PGbox3d.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "\"public\".\"geometry\"", io.github.sebasbaumh.postgis.PGgeometry.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "\"public\".\"geography\"", io.github.sebasbaumh.postgis.PGgeography.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "\"public\".\"box2d\"", io.github.sebasbaumh.postgis.PGbox2d.class });
				mrawConnectionOperation.invoke(conn, mAddDataType, null,
						new Object[] { "\"public\".\"box3d\"", io.github.sebasbaumh.postgis.PGbox3d.class });
				return;
			}
		}
		catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex)
		{
			// ignore all errors here
		}
		// PGConnection could not be found
		throw new SQLException(
				"Connection is neither an org.postgresql.PGConnection, nor a Connection wrapped around an org.postgresql.PGConnection.");
	}

	/**
	 * Registers all datatypes on the given connection.
	 * @param pgconn {@link PGConnection}
	 * @throws SQLException
	 */
	public static void registerDataTypes(PGConnection pgconn) throws SQLException
	{
		pgconn.addDataType("geometry", io.github.sebasbaumh.postgis.PGgeometry.class);
		pgconn.addDataType("geography", io.github.sebasbaumh.postgis.PGgeography.class);
		pgconn.addDataType("box2d", io.github.sebasbaumh.postgis.PGbox2d.class);
		pgconn.addDataType("box3d", io.github.sebasbaumh.postgis.PGbox3d.class);
		pgconn.addDataType("public.geometry", io.github.sebasbaumh.postgis.PGgeometry.class);
		pgconn.addDataType("public.geography", io.github.sebasbaumh.postgis.PGgeography.class);
		pgconn.addDataType("public.box2d", io.github.sebasbaumh.postgis.PGbox2d.class);
		pgconn.addDataType("public.box3d", io.github.sebasbaumh.postgis.PGbox3d.class);
		pgconn.addDataType("\"public\".\"geometry\"", io.github.sebasbaumh.postgis.PGgeometry.class);
		pgconn.addDataType("\"public\".\"geography\"", io.github.sebasbaumh.postgis.PGgeography.class);
		pgconn.addDataType("\"public\".\"box2d\"", io.github.sebasbaumh.postgis.PGbox2d.class);
		pgconn.addDataType("\"public\".\"box3d\"", io.github.sebasbaumh.postgis.PGbox3d.class);
	}

	/**
	 * Registers all datatypes for binary transfer on the given connection, supports wrapped connections.
	 * <p>
	 * NOTE: this is experimental and only necessary until PostgreSQL JDBC driver is able to register types for binary
	 * transfer.
	 * @param conn {@link Connection}
	 * @throws SQLException if the {@link Connection} is no {@link BaseConnection}.
	 */
	// FIX: this is experimental and only necessary until PostgreSQL JDBC driver is able to register types for binary
	// transfer
	// see https://github.com/pgjdbc/pgjdbc/pull/2556
	public static void registerDataTypesForBinaryTransfer(Connection conn) throws SQLException
	{
		// try to get query executor
		QueryExecutor executor = getQueryExecutor(conn);
		// collect oids of geometry types
		ArrayList<Integer> geometryOids = new ArrayList<Integer>();
		try (Statement st = conn.createStatement())
		{
			try (ResultSet rs = st
					.executeQuery("SELECT oid,typname FROM pg_type WHERE typname IN ('geometry','geography')"))
			{
				while (rs.next())
				{
					geometryOids.add(rs.getInt(1));
				}
			}
		}

		// this is kind of a hack as it currently is not possible to get already enabled oids from the connection get
		// base oids like in PgConnection
		Collection<Integer> supportedBinaryOids = Arrays.asList(Oid.BYTEA, Oid.INT2, Oid.INT4, Oid.INT8, Oid.FLOAT4,
				Oid.FLOAT8, Oid.NUMERIC, Oid.TIME, Oid.DATE, Oid.TIMETZ, Oid.TIMESTAMP, Oid.TIMESTAMPTZ,
				Oid.BYTEA_ARRAY, Oid.INT2_ARRAY, Oid.INT4_ARRAY, Oid.INT8_ARRAY, Oid.OID_ARRAY, Oid.FLOAT4_ARRAY,
				Oid.FLOAT8_ARRAY, Oid.VARCHAR_ARRAY, Oid.TEXT_ARRAY, Oid.POINT, Oid.BOX, Oid.UUID);
		Set<Integer> receiveOids = new HashSet<Integer>(supportedBinaryOids);
		Set<Integer> sendOids = new HashSet<Integer>(supportedBinaryOids);
		// do the same as the original PostgreSQL JDBC driver
		sendOids.remove(Oid.DATE);

		// add geometry oids
		receiveOids.addAll(geometryOids);
		sendOids.addAll(geometryOids);

		// finally set oids
		// TODO: this replaces the already set oids and should be changed later (when additional API functions are
		// merged to pgjdbc)
		executor.setBinaryReceiveOids(receiveOids);
		executor.setBinarySendOids(sendOids);
	}

	/**
	 * Tries to turn the given {@link Connection} into a {@link PGConnection}, supports wrapped connections and
	 * JBoss/WildFly WrappedConnections.
	 * @param conn {@link Connection}
	 * @return {@link PGConnection} on success, else null
	 * @throws SQLException
	 */
	@Nullable
	private static PGConnection tryUnwrap(Connection conn) throws SQLException
	{
		// short cut
		if (conn instanceof PGConnection)
		{
			return (PGConnection) conn;
		}
		// try to get underlying PostgreSQL connection
		if (conn.isWrapperFor(PGConnection.class))
		{
			return conn.unwrap(PGConnection.class);
		}
		// unwrap connection, e.g. in JBoss/WildFly
		try
		{
			Method method = conn.getClass().getMethod("getUnderlyingConnection");
			return (PGConnection) method.invoke(conn);
		}
		catch (Exception ex)
		{
			// just ignore exceptions
		}
		return null;
	}

	/**
	 * Check whether the driver thinks he can handle the given URL.
	 * @see java.sql.Driver#acceptsURL
	 * @param url the URL of the driver
	 * @return true if this driver accepts the given URL
	 */
	@Override
	public boolean acceptsURL(@SuppressWarnings("null") @Nonnull String url)
	{
		// try to get URL for PostgreSQL
		String mangledURL = mangleURL(url);
		if (mangledURL != null)
		{
			return super.acceptsURL(mangledURL);
		}
		// unknown URL
		return false;
	}

	/**
	 * Creates a postgresql connection, and then adds the PostGIS data types to it calling addpgtypes()
	 * @param url the URL of the database to connect to
	 * @param info a list of arbitrary tag/value pairs as connection arguments
	 * @return a connection to the URL or null if it isnt us
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.Driver#connect
	 * @see org.postgresql.Driver
	 */
	@Nullable
	@Override
	public java.sql.Connection connect(@SuppressWarnings("null") @Nonnull String url,
			@SuppressWarnings("null") @Nonnull Properties info) throws SQLException
	{
		// try to get URL for PostgreSQL
		String mangledURL = mangleURL(url);
		if (mangledURL != null)
		{
			// connect to URL
			Connection result = super.connect(mangledURL, info);
			if (result instanceof PGConnection)
			{
				// add geometry and box types
				registerDataTypes((PGConnection) result);
			}
			return result;
		}
		// unknown URL, just return null to the caller (don't throw an exception)
		return null;
	}

	@Override
	public Logger getParentLogger()
	{
		return logger;
	}
}
