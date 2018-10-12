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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A CURVEPOLYGON is just like a polygon, with an outer ring and zero or more inner rings. The difference is that a ring
 * can take the form of a circular string, linear string or compound string.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class CurvePolygon extends PolygonBase<LineString>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for polygons with curved segments.
	 */
	public static final int TYPE = 10;

	/**
	 * Constructs an instance.
	 */
	public CurvePolygon()
	{
		super(TYPE, LineString.class);
	}

	/**
	 * Constructs an instance.
	 * @param rings rings
	 */
	public CurvePolygon(Iterable<? extends LineString> rings)
	{
		super(TYPE, LineString.class, rings);
	}

}
