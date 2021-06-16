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
		Driver driver = DriverManager.getDriver("jdbc:postgresql_postGIS:/");
		Assert.assertEquals(DriverWrapper.class, driver.getClass());
	}

}