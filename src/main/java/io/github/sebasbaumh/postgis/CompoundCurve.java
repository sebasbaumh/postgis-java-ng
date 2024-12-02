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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A compound curve is a single, continuous curve that has both curved (circular) segments and linear segments. That
 * means that in addition to having well-formed components, the end point of every component (except the last) must be
 * coincident with the start point of the following component. Just note: here it is treated as a special
 * {@link MultiCurve} where the end points of all contained lines match.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class CompoundCurve extends Curve implements Iterable<LineString>
{
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for single, continuous curves that have both curved (circular) segments and linear
	 * segments.
	 */
	public static final int TYPE = 9;

	/**
	 * Sub geometries.
	 */
	private final ArrayList<LineString> subgeoms = new ArrayList<LineString>();

	/**
	 * Constructs an instance.
	 */
	public CompoundCurve()
	{
		super(TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param geoms geometries
	 */
	public CompoundCurve(Iterable<? extends LineString> geoms)
	{
		super(TYPE);
		addAll(geoms);
	}

	/**
	 * Adds a geometry.
	 * @param geom geometry
	 */
	public void add(LineString geom)
	{
		subgeoms.add(geom);
	}

	/**
	 * Adds all given geometries.
	 * @param geoms geometries
	 */
	public final void addAll(Iterable<? extends LineString> geoms)
	{
		for (LineString geom : geoms)
		{
			subgeoms.add(geom);
		}
	}

	@Override
	public boolean checkConsistency()
	{
		if (!super.checkConsistency() || subgeoms.isEmpty())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(subgeoms);
	}

	/**
	 * Closes this {@link Curve} if the last coordinate is not already the same as the first coordinate.
	 */
	@Override
	public void close()
	{
		LineString lsFirst = PostGisUtil.firstOrDefault(subgeoms);
		LineString lsLast = PostGisUtil.lastOrDefault(subgeoms);
		if ((lsFirst != null) && (lsLast != null))
		{
			Point pFirst = lsFirst.getStartPoint();
			Point pLast = lsLast.getEndPoint();
			// check if there is a first point and the last point equals the first one
			if ((pFirst != null) && (pLast != null) && !pFirst.coordsAreEqual(pLast))
			{
				// add the first point as closing last point
				lsLast.add(pFirst.copy());
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		// check type and parent
		if (other instanceof CompoundCurve) {
			CompoundCurve cother = (CompoundCurve) other;  // 手动转换类型
			if (super.equals(other)) {
				return PostGisUtil.equalsIterable(this.subgeoms, cother.subgeoms);
			}
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getCoordinates()
	 */
	@Override
	public Iterable<Point> getCoordinates()
	{
		ArrayList<Point> l = new ArrayList<Point>();
		for (LineString geom : subgeoms)
		{
			for (Point p : geom.getCoordinates())
			{
				l.add(p);
			}
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getEndPoint()
	 */
	@Nullable
	@Override
	public Point getEndPoint()
	{
		LineString ls = PostGisUtil.lastOrDefault(subgeoms);
		if (ls != null)
		{
			return ls.getEndPoint();
		}
		return null;
	}

	/**
	 * Gets all geometries.
	 * @return geometries
	 */
	public Collection<LineString> getGeometries()
	{
		return subgeoms;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getNumberOfCoordinates()
	 */
	@Override
	public int getNumberOfCoordinates()
	{
		int n = 0;
		for (LineString geom : subgeoms)
		{
			n += geom.getNumberOfCoordinates();
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getStartPoint()
	 */
	@Nullable
	@Override
	public Point getStartPoint()
	{
		LineString ls = PostGisUtil.firstOrDefault(subgeoms);
		if (ls != null)
		{
			return ls.getStartPoint();
		}
		return null;
	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + subgeoms.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#hasMeasure()
	 */
	@Override
	public boolean hasMeasure()
	{
		for (LineString geom : subgeoms)
		{
			if (geom.hasMeasure())
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#is3d()
	 */
	@Override
	public boolean is3d()
	{
		for (LineString geom : subgeoms)
		{
			if (geom.is3d())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks, if there are no sub-geometries.
	 * @return true on success, else false
	 */
	@Override
	public boolean isEmpty()
	{
		return subgeoms.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<LineString> iterator()
	{
		return subgeoms.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeom#length()
	 */
	@Override
	public double length()
	{
		double d = 0;
		for (LineString ls : subgeoms)
		{
			d += ls.length();
		}
		return d;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Curve#reverse()
	 */
	@Override
	public void reverse()
	{
		// reverse linestrings as a whole
		Collections.reverse(subgeoms);
		// then reverse all individually
		for (LineString ls : subgeoms)
		{
			ls.reverse();
		}
	}

	@Override
	public void setSrid(int srid)
	{
		super.setSrid(srid);
		for (LineString geom : subgeoms)
		{
			geom.setSrid(srid);
		}
	}

	/**
	 * Gets the number of contained geometries.
	 * @return number of contained geometries
	 */
	public int size()
	{
		return this.subgeoms.size();
	}

}
