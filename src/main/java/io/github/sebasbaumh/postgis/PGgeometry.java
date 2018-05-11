/*
 * PGgeometry.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - PGobject Geometry Wrapper
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.util.PGobject;

import io.github.sebasbaumh.postgis.binary.BinaryParser;
import io.github.sebasbaumh.postgis.binary.BinaryWriter;

/**
 * Basic geometry class.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class PGgeometry extends PGobject
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	private Geometry geom;

	/**
	 * Constructs an instance.
	 */
	@SuppressWarnings("null")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public PGgeometry()
	{
		this.setType("geometry");
	}

	/**
	 * Constructs an instance.
	 * @param geom {@link Geometry}
	 */
	public PGgeometry(Geometry geom)
	{
		this();
		this.geom = geom;
	}

	/**
	 * Constructs an instance.
	 * @param value geometry
	 * @throws SQLException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS")
	public PGgeometry(String value) throws SQLException
	{
		this();
		setValue(value);
	}

	@Override
	public Object clone()
	{
		return new PGgeometry(geom);
	}

	/**
	 * Gets the underlying {@link Geometry}.
	 * @return {@link Geometry}
	 */
	public Geometry getGeometry()
	{
		return geom;
	}

	/**
	 * Gets the OGIS geometry type.
	 * @return geometry type
	 */
	public int getGeoType()
	{
		return geom.type;
	}

	@Override
	public String getValue()
	{
		return BinaryWriter.writeHexed(geom);
	}

	/**
	 * Sets the underlying {@link Geometry}.
	 * @param newgeom {@link Geometry}
	 */
	public void setGeometry(Geometry newgeom)
	{
		this.geom = newgeom;
	}

	@Override
	public void setValue(@SuppressWarnings("null") String value) throws SQLException
	{
		geom = BinaryParser.parse(value);
	}

	@Override
	public String toString()
	{
		return geom.toString();
	}
}
