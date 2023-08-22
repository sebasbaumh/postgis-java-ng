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

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Point geometry.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class Point extends Geometry
{
	private static final long serialVersionUID = 0x100;

	/**
	 * The OGIS geometry type number for points.
	 */
	public static final int TYPE = 1;

	/**
	 * The measure of the point.
	 */
	private double m;

	/**
	 * The X coordinate of the point. In most long/lat systems, this is the longitude.
	 */
	private double x;

	/**
	 * The Y coordinate of the point. In most long/lat systems, this is the latitude.
	 */
	private double y;

	/**
	 * The Z coordinate of the point. In most long/lat systems, this is a radius from the center of the earth, or the
	 * height / elevation over the ground.
	 */
	private double z;

	/**
	 * Constructs an empty instance.
	 */
	public Point()
	{
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}

	/**
	 * Constructs a new Point
	 * @param x the longitude / x ordinate
	 * @param y the latitude / y ordinate
	 */
	public Point(double x, double y)
	{
		this(x, y, Double.NaN, Double.NaN);
	}

	/**
	 * Constructs a new Point
	 * @param x the longitude / x ordinate
	 * @param y the latitude / y ordinate
	 * @param z the radius / height / elevation / z ordinate (can be {@link Double#NaN} for no coordinate)
	 */
	public Point(double x, double y, double z)
	{
		this(x, y, z, Double.NaN);
	}

	/**
	 * Constructs a new Point
	 * @param x the longitude / x ordinate
	 * @param y the latitude / y ordinate
	 * @param z the radius / height / elevation / z ordinate (can be {@link Double#NaN} for no coordinate)
	 * @param m measure (4th dimension)
	 */
	public Point(double x, double y, double z, double m)
	{
		super(TYPE);
		this.x = x;
		this.y = y;
		this.z = z;
		this.m = m;
	}

	@Override
	public boolean checkConsistency()
	{
		return super.checkConsistency() && !Double.isNaN(this.x) && !Double.isNaN(this.y);
	}

	/**
	 * Checks it the coordinates of the given {@link Point} are equal to this {@link Point}.
	 * @param other {@link Point}
	 * @return true on success, else false
	 */
	public boolean coordsAreEqual(Point other)
	{
		return PostGisUtil.equalsDouble(x, other.x) && PostGisUtil.equalsDouble(y, other.y)
				&& (!is3d() || PostGisUtil.equalsDouble(z, other.z))
				&& (!hasMeasure() || PostGisUtil.equalsDouble(m, other.m));
	}

	/**
	 * Creates a copy of this {@link Point}.
	 * @return {@link Point}
	 */
	public Point copy()
	{
		Point p = new Point(this.x, this.y, this.z, this.m);
		p.setSrid(getSrid());
		return p;
	}

	/**
	 * Calculates the distance to the given {@link Point}.
	 * @param p {@link Point}
	 * @return distance
	 */
	public double distance(Point p)
	{
		double dX = (p.x - this.x);
		double dY = (p.y - this.y);
		double d = dX * dX + dY * dY;
		if (this.is3d() && p.is3d())
		{
			double dZ = (p.z - this.z);
			d += dZ * dZ;
		}
		return Math.sqrt(d);
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		// check type and parent
		if ((other instanceof Point p) && super.equals(other))
		{
			return coordsAreEqual(p);
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
		return Collections.singleton(this);
	}

	/**
	 * Gets the measurement.
	 * @return measurement on success, else {@link Double#NaN}
	 */
	public double getM()
	{
		return m;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getNumberOfCoordinates()
	 */
	@Override
	public int getNumberOfCoordinates()
	{
		return 1;
	}

	/**
	 * Gets the X-coordinate.
	 * @return X-coordinate on success, else {@link Double#NaN}
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * Gets the Y-coordinate.
	 * @return Y-coordinate on success, else {@link Double#NaN}
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * Gets the Z-coordinate.
	 * @return Z-coordinate on success, else {@link Double#NaN}
	 */
	public double getZ()
	{
		return z;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(x, y, z, m);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#hasMeasure()
	 */
	@Override
	public boolean hasMeasure()
	{
		return !Double.isNaN(this.m);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#is3d()
	 */
	@Override
	public boolean is3d()
	{
		return !Double.isNaN(this.z);
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return Double.isNaN(this.x) && Double.isNaN(this.y);
	}

	/**
	 * Sets the measurement.
	 * @param m measurement
	 */
	public void setM(double m)
	{
		this.m = m;
	}

	/**
	 * Sets the X-coordinate.
	 * @param x X-coordinate
	 */
	public void setX(double x)
	{
		this.x = x;
	}

	/**
	 * Sets the Y-coordinate.
	 * @param y Y-coordinate
	 */
	public void setY(double y)
	{
		this.y = y;
	}

	/**
	 * Sets the Z-coordinate.
	 * @param z Z-coordinate
	 */
	public void setZ(double z)
	{
		this.z = z;
	}

	/**
	 * Gets this {@link Point} as a 2d object.
	 * @return {@link Point}
	 */
	public Point to2d()
	{
		// create a new instance with x/y and measure (if set)
		return new Point(this.x, this.y, Double.NaN, this.m);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Point [");
		int srid = super.getSrid();
		if (srid != UNKNOWN_SRID)
		{
			sb.append("srid=");
			sb.append(srid);
			sb.append(',');
		}
		sb.append(this.x);
		sb.append(',');
		sb.append(this.y);
		if (is3d())
		{
			sb.append(',');
			sb.append(this.z);
		}
		sb.append(']');
		return sb.toString();
	}

}
