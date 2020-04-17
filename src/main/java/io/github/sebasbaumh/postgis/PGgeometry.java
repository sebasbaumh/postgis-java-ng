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

/**
 * Basic geometry class.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class PGgeometry extends PGgeometrybase
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * Constructs an instance.
	 */
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
		super(geom);
		this.setType("geometry");
	}

	/**
	 * Constructs an instance.
	 * @param value geometry
	 * @throws SQLException
	 */
	public PGgeometry(String value) throws SQLException
	{
		super(value);
		this.setType("geometry");
	}

	@Override
	public Object clone()
	{
		return new PGgeometry(geometry);
	}

}
