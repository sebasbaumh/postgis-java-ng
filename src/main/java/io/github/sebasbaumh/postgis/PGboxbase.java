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
import java.util.Objects;

import javax.annotation.Nonnull;

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
	private static void formatPoint(StringBuilder sb, Point p)
	{
		sb.append(formatCoord(p.getX()));
		sb.append(' ');
		sb.append(formatCoord(p.getY()));
		if (p.is3d())
		{
			sb.append(' ');
			sb.append(formatCoord(p.getZ()));
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
	public Object clone()
	{
		PGboxbase obj = newInstance();
		obj.llb = this.llb;
		obj.urt = this.urt;
		obj.setType(type);
		return obj;
	}

	@Override
	public boolean equals(Object obj)
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

	@Nonnull
	@Override
	public String getValue()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPrefix());
		sb.append('(');
		PGboxbase.formatPoint(sb, llb);
		sb.append(',');
		PGboxbase.formatPoint(sb, urt);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + Objects.hash(llb, urt);
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
