/*
 * Geometry.java
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

import java.io.Serializable;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The base class of all geometries
 */
@NonNullByDefault
public abstract class Geometry implements Serializable
{
	private static final long serialVersionUID = 0x100;

	/**
	 * Official UNKNOWN srid value
	 */
	public final static int UNKNOWN_SRID = 0;

	/**
	 * The spacial reference system id of this geometry, default is no srid
	 */
	private int srid = UNKNOWN_SRID;

	/**
	 * The OGIS geometry type of this feature. this is final as it never changes, it is bound to the subclass of the
	 * instance.
	 */
	private final int type;

	// WKB types:
	// POINT 1
	// LINESTRING 2
	// POLYGON 3
	// MULTIPOINT 4
	// MULTILINESTRING 5
	// MULTIPOLYGON 6
	// GEOMETRYCOLLECTION 7
	// CIRCULARSTRING 8
	// COMPOUNDCURVE 9
	// CURVEPOLYGON 10
	// MULTICURVE 11
	// MULTISURFACE 12
	// CURVE 13
	// SURFACE 14
	// POLYHEDRALSURFACE 15
	// TIN 16
	// TRIANGLE 17

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses
	 */
	protected Geometry(int type)
	{
		this.type = type;
	}

	/**
	 * Do some internal consistency checks on the geometry. Currently, all Geometries must have a valid dimension (2 or
	 * 3) and a valid type. Composed geometries must have all equal SRID, dimensionality and measures, as well as that
	 * they do not contain NULL or inconsistent subgeometries. BinaryParser and WKTParser should only generate
	 * consistent geometries. BinaryWriter may produce invalid results on inconsistent geometries.
	 * @return true if all checks are passed.
	 */
	@SuppressWarnings("static-method")
	public boolean checkConsistency()
	{
		// default is a correct geometry
		return true;
	}

	/**
	 * java.lang.Object equals implementation
	 * @param obj geometry to compare
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(@Nullable Object obj)
	{
		// short cut
		if (this == obj)
		{
			return true;
		}
		// check for type and null
		if (!(obj instanceof Geometry))
		{
			return false;
		}
		// check all properties specific to this instance
		Geometry other = (Geometry) obj;
		return other.getClass().equals(this.getClass()) && (this.type == other.type) && (this.srid == other.srid);
	}

	/**
	 * Queries the number of geometric dimensions of this geometry. This does not include measures, as opposed to the
	 * server.
	 * @deprecated use {@link #is3d()} instead
	 * @return The dimensionality (eg, 2D or 3D) of this geometry.
	 */
	@Deprecated
	public int getDimension()
	{
		return is3d() ? 3 : 2;
	}

	/**
	 * Same as getPoint(0);
	 * @return the initial Point in this geometry
	 */
	public abstract Point getFirstPoint();

	/**
	 * Same as getPoint(numPoints()-1);
	 * @return the final Point in this geometry
	 */
	public abstract Point getLastPoint();

	/**
	 * Get the nth Point of the geometry
	 * @param n the index of the point, from 0 to numPoints()-1;
	 * @return nth point in the geometry
	 * @throws ArrayIndexOutOfBoundsException in case of an emtpy geometry or bad index.
	 */
	public abstract Point getPoint(int n);

	/**
	 * The OGIS geometry type number of this geometry.
	 * @return the SRID of this geometry
	 */
	public int getSrid()
	{
		return this.srid;
	}

	/**
	 * Gets the OGIS geometry type number of this geometry.
	 * @return type of this geometry
	 */
	public int getType()
	{
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 31 * (31 + this.srid) + this.type;
	}

	/**
	 * Returns whether we have a measure (4th dimension)
	 * @return true if the geometry has a measure, false otherwise
	 */
	public abstract boolean hasMeasure();

	/**
	 * Checks if this {@link Geometry} is 3d.
	 * @return true on success, else false
	 */
	public abstract boolean is3d();

	/**
	 * Return the number of Points of the geometry
	 * @return number of points in the geometry
	 */
	public abstract int numPoints();

	/**
	 * Recursively sets the srid on this geometry and all contained subgeometries
	 * @param srid the SRID for this geometry
	 */
	public void setSrid(int srid)
	{
		this.srid = srid;
	}

}
