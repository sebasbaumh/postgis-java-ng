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

import javax.annotation.Nullable;

/**
 * Interface to mark line based geometries.
 * @author Sebastian Baumhekel
 */
public interface LineBasedGeometry
{
	/**
	 * Gets the end point.
	 * @return {@link Point} on success, else null
	 */
	@Nullable
	Point getEndPoint();

	/**
	 * Gets the start point.
	 * @return {@link Point} on success, else null
	 */
	@Nullable
	Point getStartPoint();

	/**
	 * Checks if this line is closed, so the last coordinate is the same as the first coordinate.
	 * @return true on success, else false
	 */
	boolean isClosed();

	/**
	 * Gets the length of this line.
	 * @return length
	 */
	double length();

}
