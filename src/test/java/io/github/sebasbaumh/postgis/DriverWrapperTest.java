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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test PostGIS connection.
 * @author Sebastian Baumhekel
 */
@SuppressWarnings("javadoc")
public class DriverWrapperTest extends DatabaseTestBase
{
	@Test
	public void testPooled() throws Exception
	{
		if (!hasDatabase())
		{
			return;
		}
		DataSource ds = getPooledDataSource();
		try (Connection conn = ds.getConnection())
		{
			DriverWrapper.registerDataTypes(conn);
			try (Statement st = conn.createStatement())
			{
				try (ResultSet rs = st.executeQuery("SELECT postgis_version()"))
				{
					Assert.assertTrue(rs.next());
					Assert.assertNotNull(rs.getString(1));
				}
			}
		}
		closeDataSource(ds);
	}

	// test based on https://github.com/postgis/postgis-java/pull/115
	@SuppressWarnings({ "static-method", "resource" })
	@Test
	public void testThatPostGisDoesNotOverwriteSavedExceptionForUnsupportedConnectionString()
	{
		try
		{
			DriverManager.getConnection("jdbc:missing");
		}
		catch (SQLException e)
		{
			// This should not be "Unknown protocol or subprotocol in url jdbc:missing", which
			// would indicate that PostGIS threw an exception instead of returning `null` from
			// the `connect` method for an unsupported connection string.
			// (This is documented in `java.sql.Driver.connect`.)
			//
			// The former behavior is not desirable as throwing an exception causes a previously
			// saved exception from a "better fitting" driver to be overwritten by PostGis, despite
			// PostGis not actually being able to handle the connection.
			//
			// (Imagine an Oracle connection string with a wrong password, in which the Oracle
			// driver's exception regarding the wrong password would be replaced with a generic
			// nonsensical PostGis exception.)
			Assert.assertEquals("No suitable driver found for jdbc:missing", e.getMessage());
		}
	}

	@Test
	public void testUnpooled() throws Exception
	{
		if (!hasDatabase())
		{
			return;
		}
		DataSource ds = getUnpooledDataSource();
		try (Connection conn = ds.getConnection())
		{
			DriverWrapper.registerDataTypes(conn);
			try (Statement st = conn.createStatement())
			{
				try (ResultSet rs = st.executeQuery("SELECT postgis_version()"))
				{
					Assert.assertTrue(rs.next());
					Assert.assertNotNull(rs.getString(1));
				}
			}
		}
		closeDataSource(ds);
	}

}
