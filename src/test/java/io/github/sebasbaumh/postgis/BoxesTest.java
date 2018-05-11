/*
 * BoxesTest.java
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

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({ "javadoc", "static-method" })
public class BoxesTest
{

	@Test
	public void testBox2d() throws SQLException
	{
		PGbox2d box = new PGbox2d("BOX(1 2,3 4)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.x, 0.0001);
		Assert.assertEquals(2, p0.y, 0.0001);
		Assert.assertEquals(2, p0.dimension);
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(3, p1.x, 0.0001);
		Assert.assertEquals(4, p1.y, 0.0001);
		Assert.assertEquals(2, p1.dimension);
	}

	@Test
	public void testBox3d() throws SQLException
	{
		PGbox3d box = new PGbox3d("BOX3D(1 2 3,4 5 6)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.x, 0.0001);
		Assert.assertEquals(2, p0.y, 0.0001);
		Assert.assertEquals(3, p0.z, 0.0001);
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(4, p1.x, 0.0001);
		Assert.assertEquals(5, p1.y, 0.0001);
		Assert.assertEquals(6, p1.z, 0.0001);
	}

	@Test
	public void testBox3d_2() throws SQLException
	{
		PGbox3d box = new PGbox3d("BOX3D(1 2,4 5)");
		Point p0 = box.getLLB();
		Assert.assertNotNull(p0);
		Assert.assertEquals(1, p0.x, 0.0001);
		Assert.assertEquals(2, p0.y, 0.0001);
		Assert.assertEquals(2, p0.dimension);
		Point p1 = box.getURT();
		Assert.assertNotNull(p1);
		Assert.assertEquals(4, p1.x, 0.0001);
		Assert.assertEquals(5, p1.y, 0.0001);
		Assert.assertEquals(2, p1.dimension);
	}

}