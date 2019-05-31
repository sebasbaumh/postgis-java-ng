package io.github.sebasbaumh.postgis;

import java.sql.Connection;
import java.sql.ResultSet;
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
