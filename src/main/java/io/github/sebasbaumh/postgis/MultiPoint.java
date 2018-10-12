/*
 * MultiPoint.java
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A multi point.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class MultiPoint extends MultiGeometry<Point>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for aggregate points.
	 */
	public static final int TYPE = 4;

	/**
	 * Constructs an instance.
	 */
	public MultiPoint()
	{
		super(TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param points points
	 */
	public MultiPoint(Collection<Point> points)
	{
		super(TYPE, points);
	}
}
