package io.github.sebasbaumh.postgis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test PostGIS connection.
 * @author Sebastian Baumhekel
 */
@SuppressWarnings("javadoc")
public class PostgisDatabaseTest extends DatabaseTestBase
{

	@Test
	public void test() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		try (Connection conn = getConnection())
		{
			try (Statement st = conn.createStatement())
			{
				try (ResultSet rs = st.executeQuery("SELECT postgis_version()"))
				{
					Assert.assertTrue(rs.next());
					Assert.assertNotNull(rs.getString(1));
				}
			}
		}
	}

}
