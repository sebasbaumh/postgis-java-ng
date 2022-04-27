/*
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
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 *
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package io.github.sebasbaumh.postgis;

import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.util.PGobject;

import io.github.sebasbaumh.postgis.binary.BinaryParser;
import io.github.sebasbaumh.postgis.binary.BinaryWriter;

/**
 * A PostgreSQL JDBC {@link PGobject} extension data type modeling a "geo" type. This class serves as a common
 * superclass for classes such as {@link PGgeometry} and {@link PGgeography} which model more specific type semantics.
 * @author Phillip Ross
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public abstract class PGgeometrybase extends PGobject
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	protected Geometry geometry;

	/**
	 * Constructs an instance.
	 * @param type type of this {@link PGobject}
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	protected PGgeometrybase(String type)
	{
		this.setType(type);
	}

	/**
	 * Constructs an instance.
	 * @param type type of this {@link PGobject}
	 * @param geom {@link Geometry}
	 */
	protected PGgeometrybase(String type, Geometry geom)
	{
		this.setType(type);
		this.geometry = geom;
	}

	/**
	 * Constructs an instance.
	 * @param type type of this {@link PGobject}
	 * @param value geometry
	 * @throws SQLException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS",
			"NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
	protected PGgeometrybase(String type, String value) throws SQLException
	{
		this.setType(type);
		setValue(value);
	}

	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof PGgeometrybase))
		{
			return false;
		}
		PGgeometrybase other = (PGgeometrybase) obj;
		return Objects.equals(this.geometry, other.geometry);
	}

	/**
	 * Gets the underlying {@link Geometry}.
	 * @return {@link Geometry}
	 */
	public Geometry getGeometry()
	{
		return geometry;
	}

	@Override
	public String getValue()
	{
		return BinaryWriter.writeHexed(geometry);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(geometry);
	}

	/**
	 * Sets the underlying {@link Geometry}.
	 * @param newgeom {@link Geometry}
	 */
	public void setGeometry(Geometry newgeom)
	{
		this.geometry = newgeom;
	}

	@Override
	public void setValue(@SuppressWarnings("null") @Nonnull String value) throws SQLException
	{
		geometry = BinaryParser.parse(value);
	}

	@Override
	public String toString()
	{
		return geometry.toString();
	}

}
