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

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.util.PGobject;

/**
 * Base class for bounding boxes.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
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
	 * @param type type of this {@link PGobject}
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS",
			"NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
	protected PGboxbase(String type)
	{
		this.setType(type);
	}

	/**
	 * Constructs an instance.
	 * @param type type of this {@link PGobject}
	 * @param llb lower left {@link Point}
	 * @param urt upper right {@link Point}
	 */
	protected PGboxbase(String type, Point llb, Point urt)
	{
		this.setType(type);
		this.llb = llb;
		this.urt = urt;
	}

	/**
	 * Constructs an instance.
	 * @param type type of this {@link PGobject}
	 * @param value WKT
	 * @throws SQLException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS",
			"NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
	protected PGboxbase(String type, String value) throws SQLException
	{
		this.setType(type);
		setValue(value);
	}

	/**
	 * Appends a double value to a {@link StringBuilder}. In contrast to {@link StringBuilder#append(double)} it omits a
	 * 0 decimal like "1.0" it will output "1".
	 * @param sb {@link StringBuilder}
	 * @param d double
	 */
	private static void appendDouble(StringBuilder sb, double d)
	{
		// check for fractional digits (or if the double exceeds the long range)
		if (((d % 1.0) != 0) || (d >= Long.MAX_VALUE) || (d <= Long.MIN_VALUE))
		{
			sb.append(d);
		}
		else
		{
			// omit 0-digit
			sb.append((long) d);
		}
	}

	/**
	 * Converts a point to an inner WKT string like "1 2" or "1 2 3".
	 * @param sb {@link StringBuilder}
	 * @param p {@link Point}
	 */
	private static void appendPoint(StringBuilder sb, Point p)
	{
		PGboxbase.appendDouble(sb, p.getX());
		sb.append(' ');
		PGboxbase.appendDouble(sb, p.getY());
		if (p.is3d())
		{
			sb.append(' ');
			PGboxbase.appendDouble(sb, p.getZ());
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
		List<String> tokens = PostGisUtil.split(wkt.trim(), ' ');
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
	public PGboxbase clone() throws CloneNotSupportedException
	{
		PGboxbase o = (PGboxbase) super.clone();
		o.setType(this.getType());
		o.llb = llb.copy();
		o.urt = urt.copy();
		return o;
	}

	@Override
	public boolean equals(@Nullable Object obj)
	{
		// short cut
		if (this == obj)
		{
			return true;
		}
		// check for type and null
		if (!(obj instanceof PGboxbase))
		{
			return false;
		}
		PGboxbase otherbox = (PGboxbase) obj;
		// Compare two coordinates. As the Server always returns Box3D with three dimensions, z==0 equals dimensions==2
		return (this.llb.coordsAreEqual(otherbox.llb) && this.urt.coordsAreEqual(otherbox.urt));
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
	 * The Prefix we have in WKT rep. I use an abstract method here so we do not need to replicate the String object in
	 * every instance.
	 * @return the prefix, as a string
	 */
	protected abstract String getPrefix();

	/**
	 * The OGIS geometry type number of this geometry.
	 * @return the SRID of this geometry
	 */
	public int getSrid()
	{
		return this.llb.getSrid();
	}

	/**
	 * Returns the upper right top corner of the box as a Point object
	 * @return upper right top corner of this box
	 */
	public Point getURT()
	{
		return urt;
	}

	@Nonnull
	@Override
	public String getValue()
	{
		StringBuilder sb = new StringBuilder();
		// add SRID?
		int srid = getSrid();
		if (srid != Geometry.UNKNOWN_SRID)
		{
			sb.append("SRID=");
			sb.append(srid);
			sb.append(';');
		}
		// write prefix and points
		sb.append(getPrefix());
		sb.append('(');
		PGboxbase.appendPoint(sb, llb);
		sb.append(',');
		PGboxbase.appendPoint(sb, urt);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(llb, urt);
	}

	/**
	 * Checks if this box is 3d.
	 * @return true on success, else false
	 */
	public abstract boolean is3d();

	/**
	 * Ist this box empty, so does it contain no coordinates?
	 * @return true on success, else false
	 */
	public boolean isEmpty()
	{
		return llb.isEmpty() && urt.isEmpty();
	}

	/**
	 * Recursively sets the srid on this geometry and all contained subgeometries
	 * @param srid the SRID for this geometry
	 */
	public void setSrid(int srid)
	{
		this.llb.setSrid(srid);
		this.urt.setSrid(srid);
	}

	@Override
	public void setValue(@SuppressWarnings("null") @Nonnull String value) throws SQLException
	{
		try
		{
			int srid = Geometry.UNKNOWN_SRID;
			value = value.trim();
			if (value.startsWith("SRID="))
			{
				int index = value.indexOf(';', 5); // sridprefix length is 5
				if (index < 0)
				{
					throw new SQLException("Error parsing Geometry - SRID not delimited with ';' ");
				}
				String sSrid = value.substring(5, index);
				value = value.substring(index + 1).trim();
				srid = Integer.parseInt(sSrid);
				// ensure valid SRID
				if (srid < 0)
				{
					srid = Geometry.UNKNOWN_SRID;
				}
			}
			String myPrefix = getPrefix();
			if (value.startsWith(myPrefix))
			{
				value = value.substring(myPrefix.length()).trim();
			}
			String valueNoParans = PostGisUtil.removeBrackets(value);
			List<String> tokens = PostGisUtil.split(valueNoParans, ',');
			llb = PGboxbase.pointFromWKT(tokens.get(0));
			urt = PGboxbase.pointFromWKT(tokens.get(1));
			if (srid != Geometry.UNKNOWN_SRID)
			{
				llb.setSrid(srid);
				urt.setSrid(srid);
			}
		}
		catch (NumberFormatException | IndexOutOfBoundsException ex)
		{
			throw new SQLException("Error parsing Point: " + ex.getMessage(), ex);
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
