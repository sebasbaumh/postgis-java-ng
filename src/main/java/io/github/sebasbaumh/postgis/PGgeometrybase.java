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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

import io.github.sebasbaumh.postgis.binary.BinaryParser;
import io.github.sebasbaumh.postgis.binary.BinaryWriter;

/**
 * A PostgreSQL JDBC {@link PGobject} extension data type modeling a "geo" type. This class serves as a common
 * superclass for classes such as {@link PGgeometry} and {@link PGgeography} which model more specific type semantics.
 * @author Phillip Ross
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public abstract class PGgeometrybase extends PGobject implements PGBinaryObject
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * Underlying geometry.
	 */
	@Nullable
	protected Geometry geometry;

	/**
	 * Geometry data as bytes.
	 */
	@Nullable
	private byte[] geometryData;

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
	public PGgeometrybase clone() throws CloneNotSupportedException
	{
		PGgeometrybase o = (PGgeometrybase) super.clone();
		o.setType(this.getType());
		o.setGeometry(this.getGeometry());
		return o;
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
	 * Gets the binary value.
	 * @return binary value on success, else null
	 */
	@Nullable
	private byte[] getBinaryValue()
	{
		// short cut
		if (this.geometryData != null)
		{
			return this.geometryData;
		}
		// check if geometry is there
		if (this.geometry != null)
		{
			// build geometry data and remember it
			byte[] data = BinaryWriter.writeBinary(geometry);
			this.geometryData = data;
			return data;
		}
		// no geometry
		return null;
	}

	/**
	 * Gets the underlying {@link Geometry}.
	 * @return {@link Geometry} on success, else null
	 */
	@Nullable
	public Geometry getGeometry()
	{
		return geometry;
	}

	@Nullable
	@Override
	public String getValue()
	{
		if (geometry != null)
		{
			return BinaryWriter.writeHexed(geometry);
		}
		return null;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(geometry);
	}

	@Override
	public int lengthInBytes()
	{
		byte[] data = getBinaryValue();
		if (data != null)
		{
			return data.length;
		}
		// no geometry
		return 0;
	}

	@Override
	public void setByteValue(@SuppressWarnings("null") byte[] value, int offset) throws SQLException
	{
		// parse the given bytes
		this.geometry = BinaryParser.parse(value, offset);
	}

	/**
	 * Sets the underlying {@link Geometry}.
	 * @param newgeom {@link Geometry} (can be null)
	 */
	public void setGeometry(@Nullable Geometry newgeom)
	{
		this.geometry = newgeom;
		// reset binary data
		this.geometryData = null;
	}

	@Override
	public void setValue(@SuppressWarnings("null") @Nonnull String value) throws SQLException
	{
		this.geometry = BinaryParser.parse(value);
		// reset binary data
		this.geometryData = null;
	}

	@Override
	public void toBytes(@SuppressWarnings("null") byte[] bytes, int offset)
	{
		byte[] data = getBinaryValue();
		if (data != null)
		{
			// make sure array is large enough
			if ((bytes.length - offset) <= data.length)
			{
				// copy data
				System.arraycopy(data, 0, bytes, offset, data.length);
			}
			else
			{
				throw new IllegalArgumentException(
						"byte array is too small, expected: " + data.length + " got: " + (bytes.length - offset));
			}
		}
		else
		{
			throw new IllegalStateException("no geometry has been set");
		}
	}

	@Override
	public String toString()
	{
		return String.valueOf(geometry);
	}

}
