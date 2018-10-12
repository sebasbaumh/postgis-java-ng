/*
 * CircularString.java
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The CIRCULARSTRING is the basic curve type, similar to a LINESTRING in the linear world. A single segment required
 * three points, the start and end points (first and third) and any other point on the arc. The exception to this is for
 * a closed circle, where the start and end points are the same. In this case the second point MUST be the center of the
 * arc, ie the opposite side of the circle. To chain arcs together, the last point of the previous arc becomes the first
 * point of the next arc, just like in LINESTRING. This means that a valid circular string must have an odd number of
 * points greater than 1.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class CircularString extends LineString
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for arcs/circles.
	 */
	public static final int TYPE = 8;

	/**
	 * Constructs an instance.
	 */
	public CircularString()
	{
		super(CircularString.TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param points points
	 */
	public CircularString(Iterable<Point> points)
	{
		super(CircularString.TYPE);
		addAll(points);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeom#length()
	 */
	@Override
	public double length()
	{
		// FIX: calculate length
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineString#reverse()
	 */
	@Override
	public void reverse()
	{
		// TODO Auto-generated method stub
		super.reverse();
	}

}
