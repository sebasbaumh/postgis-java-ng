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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A CURVEPOLYGON is just like a polygon, with an outer ring and zero or more inner rings. The difference is that a ring
 * can take the form of a circular string, linear string or compound string.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class CurvePolygon extends PolygonBase<Curve>
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
	 * @param lsOuterRing outer ring
	 */
	public CurvePolygon(Curve lsOuterRing)
	{
		super(TYPE, lsOuterRing);
	}

	/**
	 * Constructs an instance.
	 * @param rings rings (first one will be the outer ring)
	 */
	public CurvePolygon(Iterable<? extends Curve> rings)
	{
		super(TYPE, LineString.class, rings);
	}

}
