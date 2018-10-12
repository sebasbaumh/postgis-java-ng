/*
 * LineString.java
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
public class LineString extends Geometry implements LineBasedGeom, Iterable<Point>
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
		if (!super.checkConsistency())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(points);
	}

	/**
	 * Closes this {@link LineString} if the last coordinate is not already the same as the first coordinate.
	 */
	public void close()
	{
		if (!this.points.isEmpty())
		{
			Point pFirst = getFirstPoint();
			Point pLast = getLastPoint();
			// check if there is a first point and the last point equals the first one
			if (!pFirst.coordsAreEqual(pLast))
			{
				// add the first point as closing last point
				add(pFirst.copy());
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		// check parent
		if (super.equals(other) && (other instanceof LineString))
		{
			LineString ls = (LineString) other;
			// check all points
			return PostGisUtil.equalsIterable(this.points, ls.points);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getFirstPoint()
	 */
	@Override
	public Point getFirstPoint()
	{
		return this.points.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getLastPoint()
	 */
	@Override
	public Point getLastPoint()
	{
		return this.points.get(points.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getPoint(int)
	 */
	@Override
	public Point getPoint(int n)
	{
		return this.points.get(n);
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

	/**
	 * Checks if this LineString is closed, so the last coordinate is the same as the first coordinate.
	 * @return true on success, else false
	 */
	public boolean isClosed()
	{
		// there need to be at least 3 points to close the line
		if (points.size() > 2)
		{
			Point pFirst = getFirstPoint();
			Point pLast = getLastPoint();
			return pFirst.coordsAreEqual(pLast);
		}
		return false;
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
				p = p0;
			}
		}
		return len;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#numPoints()
	 */
	@Override
	public int numPoints()
	{
		return this.points.size();
	}

	/**
	 * Reverses this linestring.
	 */
	public void reverse()
	{
		Collections.reverse(this.points);
	}

}
