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

import org.postgresql.util.PGobject;

/**
 * Geometry class.
 * @author Sebastian Baumhekel
 */
public class PGgeometry extends PGgeometrybase
{
	/**
	 * Type of the {@link PGobject}.
	 */
	private static final String PG_TYPE = "geometry";
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * Constructs an instance.
	 */
	public PGgeometry()
	{
		super(PG_TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param geom {@link Geometry}
	 */
	public PGgeometry(Geometry geom)
	{
		super(PG_TYPE, geom);
	}

	/**
	 * Constructs an instance.
	 * @param value geometry
	 * @throws SQLException
	 */
	public PGgeometry(String value) throws SQLException
	{
		super(PG_TYPE, value);
	}

	@Override
	public Object clone()
	{
		return new PGgeometry(geometry);
	}

}
