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

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.postgresql.util.PGobject;

@SuppressWarnings({ "javadoc", "static-method" })
public class BoxesTest
{

	private static <T extends PGobject> void cloneTest(T o)
	{
		try
		{
			Object o2 = o.clone();
			Assert.assertEquals(o.getClass(), o2.getClass());
			@SuppressWarnings("unchecked")
			T t2 = (T) o2;
			Assert.assertEquals(o, o2);
			Assert.assertEquals(o.getType(), t2.getType());
			Assert.assertNotSame(o, o2);
		}
		catch (CloneNotSupportedException ex)
		{
			Assert.fail("Clone not supported: " + ex.getMessage());
		}
	}

	@Test
	public void testBox2d() throws SQLException
	{
		PGbox2d box = new PGbox2d("BOX(1 2,3 4)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.getX(), 0.0001);
		Assert.assertEquals(2, p0.getY(), 0.0001);
		Assert.assertFalse(p0.is3d());
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(3, p1.getX(), 0.0001);
		Assert.assertEquals(4, p1.getY(), 0.0001);
		Assert.assertFalse(p1.is3d());
	}

	@Test
	public void testBox3d() throws SQLException
	{
		PGbox3d box = new PGbox3d("BOX3D(1 2 3,4 5 6)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.getX(), 0.0001);
		Assert.assertEquals(2, p0.getY(), 0.0001);
		Assert.assertEquals(3, p0.getZ(), 0.0001);
		Assert.assertTrue(p0.is3d());
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(4, p1.getX(), 0.0001);
		Assert.assertEquals(5, p1.getY(), 0.0001);
		Assert.assertEquals(6, p1.getZ(), 0.0001);
		Assert.assertTrue(p1.is3d());
	}

	@Test
	public void testBox3d_2() throws SQLException
	{
		PGbox3d box = new PGbox3d("BOX3D(1 2,4 5)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.getX(), 0.0001);
		Assert.assertEquals(2, p0.getY(), 0.0001);
		Assert.assertFalse(p0.is3d());
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(4, p1.getX(), 0.0001);
		Assert.assertEquals(5, p1.getY(), 0.0001);
		Assert.assertFalse(p1.is3d());
	}

	@Test
	public void testClone() throws SQLException
	{
		cloneTest(new PGbox2d("BOX(1 2,3 4)"));
		cloneTest(new PGbox3d("BOX3D(1 2 3,4 5 6)"));
		cloneTest(new PGbox3d("BOX3D(1 2,4 5)"));
	}

}