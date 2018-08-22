/*
 * PGboxbase.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - bounding box model
 *
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

import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PGobject;

/**
 * Base class for bounding boxes.
 * @author Sebastian Baumhekel
 */
public abstract class PGboxbase extends PGobject
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * The lower left bottom corner of the box.
	 */
	protected Point llb;

	/**
	 * The upper right top corner of the box.
	 */
	protected Point urt;

	/**
	 * Constructs an instance.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS")
	protected PGboxbase()
	{
		this.setType(getPGtype());
	}

	/**
	 * Constructs an instance.
	 * @param llb lower left {@link Point}
	 * @param urt upper right {@link Point}
	 */
	protected PGboxbase(Point llb, Point urt)
	{
		this();
		this.llb = llb;
		this.urt = urt;
	}

	/**
	 * Constructs an instance.
	 * @param value WKT
	 * @throws SQLException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS")
	protected PGboxbase(String value) throws SQLException
	{
		this();
		setValue(value);
	}

	/**
	 * Compare two coordinates with lazy dimension checking. As the Server always returns Box3D with three dimensions,
	 * z==0 equals dimensions==2
	 * @param first First of two points to be compared
	 * @param second Second of two points to be compared
	 * @return true if the points are the same, false otherwise
	 */
	private static boolean compareLazyDim(Point first, Point second)
	{
		return first.x == second.x && first.y == second.y
				&& (((first.dimension == 2 || first.z == 0.0) && (second.dimension == 2 || second.z == 0))
						|| (first.z == second.z));
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
	 * Converts a point to an inner WKT string like "1 2" or "1 2 3".
	 * @param sb {@link StringBuilder}
	 * @param p {@link Point}
	 */
	private static void formatPoint(StringBuffer sb, Point p)
	{
		sb.append(formatCoord(p.x));
		sb.append(' ');
		sb.append(formatCoord(p.y));
		if (p.dimension == 3)
		{
			sb.append(' ');
			sb.append(formatCoord(p.z));
		}
	}

	/**
	 * Gets a point from an inner WKT string like "1 2" or "1 2 3".
	 * @param wkt WKT
	 * @return {@link Point} on success, else null
	 * @throws NumberFormatException if a coordinate is invalid
	 */
	private static Point pointFromWKT(String wkt)
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

	/**
	 * Splits a String at the first occurrence of border character. Poor man's String.split() replacement, as
	 * String.split() was invented at jdk1.4, and the Debian PostGIS Maintainer had problems building the woody backport
	 * of his package using DFSG-free compilers. In all the cases we used split() in the io.github.sebasbaumh.postgis
	 * package, we only needed to split at the first occurence, and thus this code could even be faster.
	 * @param whole the String to be split
	 * @return String array containing the split elements
	 * @throws SQLException when a SQLException occurrs
	 */
	private static String[] splitSRID(String whole) throws SQLException
	{
		int index = whole.indexOf(';', 5); // sridprefix length is 5
		if (index == -1)
		{
			throw new SQLException("Error parsing Geometry - SRID not delimited with ';' ");
		}
		else
		{
			return new String[] { whole.substring(0, index), whole.substring(index + 1) };
		}
	}

	@Override
	public Object clone()
	{
		PGboxbase obj = newInstance();
		obj.llb = this.llb;
		obj.urt = this.urt;
		obj.setType(type);
		return obj;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof PGboxbase)
		{
			PGboxbase otherbox = (PGboxbase) other;
			return (compareLazyDim(this.llb, otherbox.llb) && compareLazyDim(this.urt, otherbox.urt));
		}
		return false;
	}

	/**
	 * Returns the lower left bottom corner of the box as a Point object
	 * @return lower left bottom corner of this box
	 */
	public Point getLLB()
	{
		return llb;
	}

	/**
	 * The Postgres type we have (same construct as getPrefix())
	 * @return String containing the name of the type for this box.
	 */
	public abstract String getPGtype();

	/**
	 * The Prefix we have in WKT rep. I use an abstract method here so we do not need to replicate the String object in
	 * every instance.
	 * @return the prefix, as a string
	 */
	protected abstract String getPrefix();

	/**
	 * Returns the upper right top corner of the box as a Point object
	 * @return upper right top corner of this box
	 */
	public Point getURT()
	{
		return urt;
	}

	@Override
	public String getValue()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix());
		sb.append('(');
		PGboxbase.formatPoint(sb, llb);
		sb.append(',');
		PGboxbase.formatPoint(sb, urt);
		sb.append(')');
		return sb.toString();
	}

	/**
	 * Obtain a new instance of a PGboxbase We could have used this.getClass().newInstance() here, but this forces us
	 * dealing with InstantiationException and IllegalAccessException. Due to the PGObject.clone() brokennes that does
	 * not allow clone() to throw CloneNotSupportedException, we cannot even pass this exceptions down to callers in a
	 * sane way.
	 * @return a new instance of PGboxbase
	 */
	protected abstract PGboxbase newInstance();

	@Override
	public void setValue(String value) throws SQLException
	{
		int srid = Geometry.UNKNOWN_SRID;
		value = value.trim();
		if (value.startsWith("SRID="))
		{
			String[] temp = PGboxbase.splitSRID(value);
			value = temp[1].trim();
			srid = Geometry.parseSRID(Integer.parseInt(temp[0].substring(5)));
		}
		String myPrefix = getPrefix();
		if (value.startsWith(myPrefix))
		{
			value = value.substring(myPrefix.length()).trim();
		}
		String valueNoParans = GeometryTokenizer.removeLeadingAndTrailingStrings(value, "(", ")");
		List<String> tokens = GeometryTokenizer.tokenize(valueNoParans, ',');
		try
		{
			llb = PGboxbase.pointFromWKT(tokens.get(0));
			urt = PGboxbase.pointFromWKT(tokens.get(1));
			if (srid != Geometry.UNKNOWN_SRID)
			{
				llb.setSrid(srid);
				urt.setSrid(srid);
			}
		}
		catch (NumberFormatException ex)
		{
			throw new SQLException("Error parsing Point: " + ex, ex);
		}
	}

	/**
	 * Unlike geometries, toString() does _not_ contain the srid, as server-side PostGIS cannot parse this.
	 * @return String representation of this box
	 */
	@Override
	public String toString()
	{
		return getValue();
	}
}
