/*
 * Polygon.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - geometry model
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

import java.util.Collection;

/**
 * A polygon.
 * @author Sebastian Baumhekel
 */
public class Polygon extends PolygonBase<LinearRing>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	public Polygon()
	{
		super(POLYGON);
	}

	public Polygon(Collection<LinearRing> rings)
	{
		super(POLYGON, rings);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#numPoints()
	 */
	@Override
	public int numPoints()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getPoint(int)
	 */
	@Override
	public Point getPoint(int n)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getFirstPoint()
	 */
	@Override
	public Point getFirstPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getLastPoint()
	 */
	@Override
	public Point getLastPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
