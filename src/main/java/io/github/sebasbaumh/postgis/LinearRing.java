/*
 * LinearRing.java
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
 * This represents the LinearRing GIS datatype. This type is used to construct the polygon types, but is not stored or
 * retrieved directly from the database.
 */
@NonNullByDefault
public class LinearRing extends LineString
{
	private static final long serialVersionUID = 0x100;
	// Fake type for linear ring
	private static final int TYPE = 0;

	/**
	 * Constructs an instance.
	 */
	public LinearRing()
	{
		super(LinearRing.TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param points points
	 */
	public LinearRing(Iterable<Point> points)
	{
		super(LinearRing.TYPE, points);
	}

}