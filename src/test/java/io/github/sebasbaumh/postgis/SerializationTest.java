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

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class SerializationTest extends DatabaseTestBase
{

	@Test
	public void serializationCheckPGgeometry() throws Exception
	{
		if (!hasDatabase())
		{
			return;
		}
		try
		{
			new ObjectOutputStream(new ByteArrayOutputStream())
					.writeObject(new PGgeometry(getWKBFromWKT("MULTIPOLYGON(((1 1,1 2,2 1,1 1)))")));
		}
		catch (NotSerializableException ex)
		{
			Assert.fail("serialization of PGgeometry failed: " + ex);
		}
	}

}
