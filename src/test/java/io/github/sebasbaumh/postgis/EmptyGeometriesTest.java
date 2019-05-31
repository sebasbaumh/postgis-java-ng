/*
 * EmptyGeometriesTest.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains tests for handling of empty geometries.
 * @author Phillip Ross {@literal <phillip.r.g.ross@gmail.com>}
 */
@SuppressWarnings("javadoc")
public class EmptyGeometriesTest extends DatabaseTestBase
{
	private static final String[] castTypes = new String[] { "bytea", "text", "geometry" };

	private static final String[] geometriesToTest = new String[] { "POINT", "LINESTRING", "POLYGON", "MULTIPOINT",
			"MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION", };

	private static final Logger logger = LoggerFactory.getLogger(EmptyGeometriesTest.class);

	private Connection connection = null;

	private Statement statement = null;

	private static List<String> generateSqlStatements()
	{
		List<String> sqlStatementList = new ArrayList<>();
		for (String geometry : geometriesToTest)
		{
			StringBuilder stringBuilder = new StringBuilder("select ");
			for (String castType : castTypes)
			{
				stringBuilder.append("geometry_in('").append(geometry).append(" EMPTY')::").append(castType)
						.append(", ");
			}
			String sqlStatement = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
			logger.debug("generate sql statement: {}", sqlStatement);
			sqlStatementList.add(sqlStatement);
		}
		return sqlStatementList;
	}

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
	public void testSqlStatements() throws SQLException
	{
		if (!hasDatabase())
		{
			return;
		}
		for (String sqlStatement : generateSqlStatements())
		{
			logger.debug("**********");
			logger.debug("* Executing sql statemnent => [{}]", sqlStatement);
			logger.debug("**********");
			try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
					ResultSet resultSet = preparedStatement.executeQuery())
			{
				resultSet.next();
				for (int i = 1; i <= 3; i++)
				{
					Object resultSetObject = resultSet.getObject(i);
					logger.debug("returned resultSetObject {} => (class=[{}]) {}", i,
							resultSetObject.getClass().getName(), resultSetObject);
				}
				resultSet.close();
			}
		}
	}

}