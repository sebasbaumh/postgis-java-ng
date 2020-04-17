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

import javax.annotation.Nonnull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.postgresql.util.PGobject;

import io.github.sebasbaumh.postgis.binary.BinaryParser;
import io.github.sebasbaumh.postgis.binary.BinaryWriter;

/**
 * A PostgreSQL JDBC PGobject extension data type modeling a "geo" type. This class serves as a common superclass for
 * classes such as PGgeometry and PGgeography which model more specific type semantics.
 * @author Phillip Ross
 */
@NonNullByDefault
public abstract class PGgeometrybase extends PGobject
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	protected Geometry geometry;

	/**
	 * Constructs an instance.
	 */
	@SuppressWarnings("null")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	protected PGgeometrybase()
	{
	}

	/**
	 * Constructs an instance.
	 * @param geom {@link Geometry}
	 */
	protected PGgeometrybase(Geometry geom)
	{
		this.geometry = geom;
	}

	/**
	 * Constructs an instance.
	 * @param value geometry
	 * @throws SQLException
	 */
	@SuppressWarnings("null")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS")
	protected PGgeometrybase(String value) throws SQLException
	{
		setValue(value);
	}

	/**
	 * Gets the underlying {@link Geometry}.
	 * @return {@link Geometry}
	 */
	public Geometry getGeometry()
	{
		return geometry;
	}

	/**
	 * Gets the OGIS geometry type.
	 * @return geometry type
	 */
	public int getGeoType()
	{
		return geometry.getType();
	}

	@Override
	public String getValue()
	{
		return BinaryWriter.writeHexed(geometry);
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
