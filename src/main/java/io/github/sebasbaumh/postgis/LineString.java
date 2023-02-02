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
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Linestring.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class LineString extends Curve implements Iterable<Point>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * The OGIS geometry type number for lines.
	 */
	public static final int TYPE = 2;

	private final ArrayList<Point> points = new ArrayList<Point>();

	/**
	 * Constructs an instance.
	 */
	public LineString()
	{
		super(TYPE);
	}

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses.
	 */
	protected LineString(int type)
	{
		super(type);
	}

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses.
	 * @param points {@link Point}s
	 */
	protected LineString(int type, Iterable<Point> points)
	{
		super(type);
		addAll(points);
	}

	/**
	 * Constructs an instance.
	 * @param points points
	 */
	public LineString(Iterable<Point> points)
	{
		super(LineString.TYPE);
		addAll(points);
	}

	/**
	 * Adds the given point.
	 * @param p point
	 */
	public void add(Point p)
	{
		points.add(p);
	}

	/**
	 * Adds all given points.
	 * @param geoms points
	 */
	public final void addAll(Iterable<Point> geoms)
	{
		for (Point geom : geoms)
		{
			points.add(geom);
		}
	}

	@Override
	public boolean checkConsistency()
	{
		if (!super.checkConsistency() || points.isEmpty())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(points);
	}

	/**
	 * Closes this {@link LineString} if the last coordinate is not already the same as the first coordinate.
	 */
	@Override
	public void close()
	{
		Point pFirst = getStartPoint();
		Point pLast = getEndPoint();
		// check if there is a first point and the last point equals the first one
		if ((pFirst != null) && (pLast != null) && !pFirst.coordsAreEqual(pLast))
		{
			// add the first point as closing last point
			add(pFirst.copy());
		}
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		// check type and parent
		if ((other instanceof LineString) && super.equals(other))
		{
			LineString ls = (LineString) other;
			// check all points
			return PostGisUtil.equalsIterable(this.points, ls.points);
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
		return this.points;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getEndPoint()
	 */
	@Nullable
	@Override
	public Point getEndPoint()
	{
		return PostGisUtil.lastOrDefault(points);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getNumberOfCoordinates()
	 */
	@Override
	public int getNumberOfCoordinates()
	{
		return this.points.size();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getStartPoint()
	 */
	@Nullable
	@Override
	public Point getStartPoint()
	{
		return PostGisUtil.firstOrDefault(points);
	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + points.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#hasMeasure()
	 */
	@Override
	public boolean hasMeasure()
	{
		for (Point geom : points)
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
		for (Point geom : points)
		{
			if (geom.is3d())
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.points.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Point> iterator()
	{
		return this.points.iterator();
	}

	@Override
	public double length()
	{
		double len = 0;
		if (points.size() > 1)
		{
			Point p0 = points.get(0);
			for (int i = 1; i < points.size(); i++)
			{
				Point p = points.get(i);
				len += p0.distance(p);
				p0 = p;
			}
		}
		return len;
	}

	/**
	 * Reverses this linestring.
	 */
	@Override
	public void reverse()
	{
		Collections.reverse(this.points);
	}

}
