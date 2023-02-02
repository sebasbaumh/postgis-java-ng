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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests to ensure that the drivers that are registered as services in META-INF/services/java.sql.Driver are resolved
 * correctly. Ported from postgis-java.
 */
@SuppressWarnings({ "static-method", "javadoc" })
public class ServiceTest
{

	@Test
	public void testWrapperService() throws SQLException
	{
		String jdbcUrl = System.getProperty(DatabaseTestBase.CONFIG_JDBC_URL);
		if (jdbcUrl == null)
		{
			System.out.println("Tests are running without a database");
			return;
		}

		if (jdbcUrl.startsWith(DriverWrapper.POSTGRES_PROTOCOL))
		{
			jdbcUrl = DriverWrapper.POSTGIS_PROTOCOL + jdbcUrl.substring(DriverWrapper.POSTGRES_PROTOCOL.length());
		}
		else
		{
			throw new SQLException("Unknown protocol or subprotocol in url: " + jdbcUrl);
		}
		Driver driver = DriverManager.getDriver(jdbcUrl);
		Assert.assertEquals(DriverWrapper.class, driver.getClass());
	}

}