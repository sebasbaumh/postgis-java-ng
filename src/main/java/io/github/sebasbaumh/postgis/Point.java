/*
 * Point.java
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

import java.util.List;
import java.util.Objects;

/**
 * Point geometry.
 * @author Sebastian Baumhekel
 */
public class Point extends Geometry
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * The measure of the point.
	 */
	public double m = 0.0;

	/**
	 * The X coordinate of the point. In most long/lat systems, this is the longitude.
	 */
	public double x;

	/**
	 * The Y coordinate of the point. In most long/lat systems, this is the latitude.
	 */
	public double y;

	/**
	 * The Z coordinate of the point. In most long/lat systems, this is a radius from the center of the earth, or the
	 * height / elevation over the ground.
	 */
	public double z;

	/**
	 * Constructs an empty instance.
	 */
	public Point()
	{
		super(POINT);
	}

	/**
	 * Constructs a new Point
	 * @param x the longitude / x ordinate
	 * @param y the latitude / y ordinate
	 */
	public Point(double x, double y)
	{
		this();
		this.x = x;
		this.y = y;
		this.z = 0.0;
		dimension = 2;
	}

	/**
	 * Constructs a new Point
	 * @param x the longitude / x ordinate
	 * @param y the latitude / y ordinate
	 * @param z the radius / height / elevation / z ordinate
	 */
	public Point(double x, double y, double z)
	{
		this();
		this.x = x;
		this.y = y;
		this.z = z;
		dimension = 3;
	}

	private static boolean double_equals(double a, double b)
	{
		if (Double.isNaN(a) && Double.isNaN(b))
		{
			return true;
		}
		return (a == b);
	}

	/**
	 * Formats a coordinate to a string omitting empty decimal places.
	 * @param d coordinate
	 * @return string
	 */
	private static String formatCoord(double d)
	{
		if (d % 1.0 != 0)
		{
			return String.format("%s", d);
		}
		return String.format("%.0f", d);
	}

	/**
	 * Gets a point from an inner WKT string like "1 2" or "1 2 3".
	 * @param wkt WKT
	 * @return {@link Point} on success, else null
	 * @throws NumberFormatException if a coordinate is invalid
	 */
	public static Point fromInnerWKT(String wkt)
	{
		List<String> tokens = GeometryTokenizer.tokenize(wkt.trim(), ' ');
		double x = Double.parseDouble(tokens.get(0));
		double y = Double.parseDouble(tokens.get(1));
		// 3d?
		if (tokens.size() == 3)
		{
			double z = Double.parseDouble(tokens.get(2));
			return new Point(x, y, z);
		}
		return new Point(x, y);
	}

	@Override
	public boolean checkConsistency()
	{
		return super.checkConsistency() && (this.dimension == 3 || this.z == 0.0)
				&& (this.haveMeasure || this.m == 0.0);
	}

	public boolean coordsAreEqual(Point other)
	{
		boolean xequals = double_equals(x, other.x);
		boolean yequals = double_equals(y, other.y);
		boolean zequals = ((dimension == 2) || double_equals(z, other.z));
		boolean mequals = ((haveMeasure == false) || double_equals(m, other.m));
		boolean result = xequals && yequals && zequals && mequals;
		return result;
	}

	public double distance(Point other)
	{
		double tx, ty, tz;
		if (this.dimension != other.dimension)
		{
			throw new IllegalArgumentException("Points have different dimensions!");
		}
		tx = this.x - other.x;
		switch (this.dimension)
		{
			case 1:
				return Math.sqrt(tx * tx);
			case 2:
				ty = this.y - other.y;
				return Math.sqrt(tx * tx + ty * ty);
			case 3:
				ty = this.y - other.y;
				tz = this.z - other.z;
				return Math.sqrt(tx * tx + ty * ty + tz * tz);
			default:
				throw new IllegalArgumentException("Illegal dimension of Point" + this.dimension);
		}
	}

	@Override
	protected boolean equalsintern(Geometry otherg)
	{
		Point other = (Point) otherg;
		return coordsAreEqual(other);
	}

	/** Optimized versions for this special case */
	@Override
	public Point getFirstPoint()
	{
		return this;
	}

	/** Optimized versions for this special case */
	@Override
	public Point getLastPoint()
	{
		return this;
	}

	public double getM()
	{
		return m;
	}

	@Override
	public Point getPoint(int index)
	{
		if (index == 0)
		{
			return this;
		}
		else
		{
			throw new ArrayIndexOutOfBoundsException("Point only has a single Point! " + index);
		}
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(x, y, z, m);
	}

	@Override
	public int numPoints()
	{
		return 1;
	}

	public void setM(double m)
	{
		haveMeasure = true;
		this.m = m;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public void setZ(double z)
	{
		this.z = z;
	}

	/**
	 * Converts a point to an inner WKT string like "1 2" or "1 2 3".
	 * @param sb {@link StringBuilder}
	 */
	public void toInnerWKT(StringBuffer sb)
	{
		sb.append(formatCoord(x));
		sb.append(' ');
		sb.append(formatCoord(y));
		if (dimension == 3)
		{
			sb.append(' ');
			sb.append(formatCoord(z));
		}
		if (haveMeasure)
		{
			sb.append(' ');
			sb.append(formatCoord(m));
		}
	}
}
