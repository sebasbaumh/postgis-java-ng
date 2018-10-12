/*
 * MultiCurve.java
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
 * The MULTICURVE is a collection of curves, which can include linear strings, circular strings or compound strings. It
 * only specifies a type of {@link Geometry} as it could contain {@link LineString}s and {@link CompoundCurve}s.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class MultiCurve extends MultiGeometry<Geometry>
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
	 */
	public <T extends Geometry> MultiCurve(Iterable<T> lines)
	{
		super(TYPE);
		for (T geom : lines)
		{
			checkCurveGeometryType(geom);
			add(geom);
		}
	}

	/**
	 * Checks the given geometry type.
	 * @param geom geometry
	 * @throws IllegalArgumentException if geometry type is not a curve
	 */
	private static void checkCurveGeometryType(Geometry geom)
	{
		// check linestrings
		if (!(geom instanceof LineString) && !(geom instanceof CompoundCurve))
		{
			throw new IllegalArgumentException("given geometry is no LineString or CompoundCurve: " + geom);
		}
	}

	/**
	 * Adds a line.
	 * @param ls line
	 */
	public void add(CompoundCurve ls)
	{
		super.add(ls);
	}

	/**
	 * Adds a line.
	 * @param ls line
	 */
	public void add(LineString ls)
	{
		super.add(ls);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.MultiGeometry#addAll(java.lang.Iterable)
	 */
	@Override
	public void addAll(Iterable<? extends Geometry> geoms)
	{
		for (Geometry geom : geoms)
		{
			checkCurveGeometryType(geom);
			super.add(geom);
		}
	}

}
