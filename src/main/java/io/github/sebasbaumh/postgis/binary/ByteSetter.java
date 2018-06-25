/*
 * ByteSetter.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - Binary Parser
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

package io.github.sebasbaumh.postgis.binary;

public class ByteSetter
{
	/**
	 * Characters for converting data to hex strings.
	 */
	private static final char[] HEX_CHAR = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };
	private final StringBuilder sb = new StringBuilder();

	public ByteSetter()
	{
	}

	/**
	 * Writes a byte.
	 * @param b byte value to set with
	 */
	public void write(byte b)
	{
		sb.append(HEX_CHAR[(b >>> 4) & 0xF]);
		sb.append(HEX_CHAR[b & 0xF]);
	}

	@Override
	public String toString()
	{
		return sb.toString();
	}
}
