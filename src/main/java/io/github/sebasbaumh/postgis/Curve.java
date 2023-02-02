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
 * Base class for simple curves like {@link LineString}s and complex classes like {@link CompoundCurve}s.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public abstract class Curve extends Geometry implements LineBasedGeometry
{
	private static final long serialVersionUID = 0x100;

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses
	 */
	protected Curve(int type)
	{
		super(type);
	}

	/**
	 * Closes this {@link Curve} if the last coordinate is not already the same as the first coordinate.
	 */
	public abstract void close();

	/**
	 * Checks if this ring is oriented in clockwise direction.
	 * @return true on success, else false
	 */
	public boolean isClockwise()
	{
		return isClosed() && (PostGisUtil.calcAreaSigned(getCoordinates()) < 0);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#isClosed()
	 */
	@Override
	public boolean isClosed()
	{
		Point pFirst = getStartPoint();
		Point pLast = getEndPoint();
		return (pFirst != null) && (pLast != null) && pFirst.coordsAreEqual(pLast);
	}

	/**
	 * Reverses this linestring.
	 */
	public abstract void reverse();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " [" + this.getNumberOfCoordinates() + " points]";
	}

}
