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
 * The MULTICURVE is a collection of curves, which can include linear strings, circular strings or compound strings. It
 * only specifies a type of {@link Geometry} as it could contain {@link LineString}s and {@link CompoundCurve}s.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class MultiCurve extends MultiGeometry<Curve>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for aggregate curves, which can include linear strings, circular strings or
	 * compound strings.
	 */
	public static final int TYPE = 11;

	/**
	 * Constructs an instance.
	 */
	public MultiCurve()
	{
		super(TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param lines lines
	 * @throws IllegalArgumentException if given lines are not of a curve type
	 */
	public MultiCurve(Iterable<Curve> lines)
	{
		super(TYPE);
		for (Curve geom : lines)
		{
			add(geom);
		}
	}

	/**
	 * Gets the length of this line.
	 * @return length
	 */
	public double length()
	{
		double d = 0;
		for (Geometry ls : subgeoms)
		{
			if (ls instanceof LineBasedGeometry)
			{
				d += ((LineBasedGeometry) ls).length();
			}
		}
		return d;
	}

}
