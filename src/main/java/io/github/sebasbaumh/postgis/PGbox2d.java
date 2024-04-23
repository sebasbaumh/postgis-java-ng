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
 * BOX2D representing the maximum extents of the geometry.
 * @author Sebastian Baumhekel
 */
public class PGbox2d extends PGboxbase
{
	/**
	 * Type of the {@link PGobject}.
	 */
	private static final String PG_TYPE = "box2d";
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * Constructs an instance.
	 */
	public PGbox2d()
	{
		super(PG_TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param llb lower-left point
	 * @param urt upper-right point
	 */
	public PGbox2d(Point llb, Point urt)
	{
		super(PG_TYPE, llb, urt);
	}

	/**
	 * Constructs an instance.
	 * @param value WKT
	 * @throws SQLException
	 */
	public PGbox2d(String value) throws SQLException
	{
		super(PG_TYPE, value);
	}

	@Override
	public PGbox2d clone() throws CloneNotSupportedException
	{
		return (PGbox2d)super.clone();
	}

	@Override
	public String getPrefix()
	{
		return "BOX";
	}

	@Override
	public boolean is3d()
	{
		return false;
	}

	@Override
	public void setValue(String value) throws SQLException
	{
		super.setValue(value);
		// force 2 dimensions
		if (llb.is3d())
		{
			llb = llb.to2d();
		}
		if (urt.is3d())
		{
			urt = urt.to2d();
		}
	}
}
