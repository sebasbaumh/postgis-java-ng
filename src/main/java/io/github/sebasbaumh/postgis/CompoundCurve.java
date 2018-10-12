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

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A compound curve is a single, continuous curve that has both curved (circular) segments and linear segments. That
 * means that in addition to having well-formed components, the end point of every component (except the last) must be
 * coincident with the start point of the following component. Just note: here it is treated as a special
 * {@link MultiCurve} where the end points of all contained lines match.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class CompoundCurve extends MultiGeometry<Geometry>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for single, continuous curves that have both curved (circular) segments and linear
	 * segments.
	 */
	public static final int TYPE = 9;

	/**
	 * Constructs an instance.
	 */
	public CompoundCurve()
	{
		super(CompoundCurve.TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param geoms geometries
	 */
	public CompoundCurve(Collection<? extends Geometry> geoms)
	{
		super(CompoundCurve.TYPE, geoms);
		// FIX: check curves
	}

}
