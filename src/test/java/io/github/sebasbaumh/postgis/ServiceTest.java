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