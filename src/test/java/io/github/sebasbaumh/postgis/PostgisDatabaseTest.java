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
