/*
 * ValueSetter.java
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

import io.github.sebasbaumh.postgis.PostGisUtil;

/**
 * Allows writing values as a string in little endian encoding and hex format.
 * @author Sebastian Baumhekel
 */
public class ValueSetter
{
	private final StringBuilder sb = new StringBuilder();

	/**
	 * Constructs an instance.
	 */
	public ValueSetter()
	{
	}

	/**
	 * Sets a byte.
	 * @param b byte value to set with
	 */
	public void setByte(byte b)
	{
		sb.append(PostGisUtil.HEX_CHAR[(b >> 4) & 0xF]);
		sb.append(PostGisUtil.HEX_CHAR[b & 0xF]);
	}

	/**
	 * Writes a double.
	 * @param data double value to be set with
	 */
	public void setDouble(double data)
	{
		setLong(Double.doubleToLongBits(data));
	}

	/**
	 * Sets a 32-Bit integer
	 * @param value int value to be set with
	 */
	public void setInt(int value)
	{
		setByte((byte) value);
		setByte((byte) (value >> 8));
		setByte((byte) (value >> 16));
		setByte((byte) (value >> 24));
	}

	/**
	 * Sets a long value. This is not needed directly, but as a nice side-effect from setDouble.
	 * @param value value value to be set with
	 */
	public void setLong(long value)
	{
		setByte((byte) value);
		setByte((byte) (value >> 8));
		setByte((byte) (value >> 16));
		setByte((byte) (value >> 24));
		setByte((byte) (value >> 32));
		setByte((byte) (value >> 40));
		setByte((byte) (value >> 48));
		setByte((byte) (value >> 56));
	}

	/**
	 * Gets the string in little endian encoding and hex format.
	 * @return string in little endian encoding and hex format.
	 */
	@Override
	public String toString()
	{
		return sb.toString();
	}

}
