/*
 * ServerTest.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 *
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("javadoc")
public class ServerTest extends DatabaseTest
{

	private static final String DATABASE_TABLE_NAME_PREFIX = "jdbc_test";
	private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);
	private Connection connection = null;
	private Statement statement = null;

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.DatabaseTest#afterDatabaseSetup()
	 */
	@Override
	protected void afterDatabaseSetup() throws SQLException
	{
		connection = getConnection();
		statement = connection.createStatement();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.DatabaseTest#beforeDatabaseShutdown()
	 */
	@Override
	protected void beforeDatabaseShutdown() throws SQLException
	{
		if ((statement != null) && (!statement.isClosed()))
		{
			statement.close();
		}
		if ((connection != null) && (!connection.isClosed()))
		{
			connection.close();
		}
	}

	@Test
	public void testServer() throws Exception
	{
		String dbtable = DATABASE_TABLE_NAME_PREFIX + "_" + UUID.randomUUID().toString().replaceAll("-", "");

		String dropSQL = "drop table " + dbtable;
		String createSQL = "create table " + dbtable + " (geom geometry, id int4)";
		String insertPointSQL = "insert into " + dbtable + " values ('POINT (10 10 10)',1)";
		String insertPolygonSQL = "insert into " + dbtable
				+ " values ('POLYGON ((0 0 0,0 10 0,10 10 0,10 0 0,0 0 0))',2)";

		logger.debug("Adding geometric type entries...");
		((org.postgresql.PGConnection) connection).addDataType("geometry", PGgeometry.class);
		((org.postgresql.PGConnection) connection).addDataType("box3d", PGbox3d.class);

		logger.debug("Creating table with geometric types...");
		boolean tableExists = false;
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		try (ResultSet resultSet = databaseMetaData.getTables(null, null, dbtable.toLowerCase(),
				new String[] { "TABLE" }))
		{
			while (resultSet.next())
			{
				tableExists = true;
			}
		}
		if (tableExists)
		{
			statement.execute(dropSQL);
		}
		statement.execute(createSQL);

		logger.debug("Inserting point...");
		statement.execute(insertPointSQL);

		logger.debug("Inserting polygon...");
		statement.execute(insertPolygonSQL);

		logger.debug("Querying table...");
		try (ResultSet resultSet = statement.executeQuery("select ST_AsText(geom),id from " + dbtable))
		{
			while (resultSet.next())
			{
				Object obj = resultSet.getObject(1);
				int id = resultSet.getInt(2);
				logger.debug("Row {}: {}", id, obj.toString());
			}
		}
	}

}