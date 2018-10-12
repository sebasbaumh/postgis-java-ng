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
 * Allows writing values in little endian encoding.
 * @author sbaumhekel
 */
public class ValueSetter
{
	private final ByteSetter data;

	/**
	 * Constructs an instance on the given {@link ByteSetter}.
	 * @param data {@link ByteSetter}
	 */
	public ValueSetter(ByteSetter data)
	{
		this.data = data;
	}

	/**
	 * Gets the endian encoding.
	 * @return endian encoding
	 */
	@SuppressWarnings("static-method")
	public byte getEndian()
	{
		return PostGisUtil.LITTLE_ENDIAN;
	}

	/**
	 * Set a byte, should be equal for all endians
	 * @param value byte value to be set with.
	 */
	public void setByte(byte value)
	{
		data.write(value);
	}

	/**
	 * Set a double.
	 * @param data double value to be set with
	 */
	public void setDouble(double data)
	{
		setLong(Double.doubleToLongBits(data));
	}

	/**
	 * Set a 32-Bit integer
	 * @param value int value to be set with
	 */
	public void setInt(int value)
	{
		data.write((byte) value);
		data.write((byte) (value >>> 8));
		data.write((byte) (value >>> 16));
		data.write((byte) (value >>> 24));
	}

	/**
	 * Set a long value. This is not needed directly, but as a nice side-effect from setDouble.
	 * @param value value value to be set with
	 */
	public void setLong(long value)
	{
		data.write((byte) value);
		data.write((byte) (value >>> 8));
		data.write((byte) (value >>> 16));
		data.write((byte) (value >>> 24));
		data.write((byte) (value >>> 32));
		data.write((byte) (value >>> 40));
		data.write((byte) (value >>> 48));
		data.write((byte) (value >>> 56));
	}

	@Override
	public String toString()
	{
		return "ValueSetter('" + data + "')";
	}
}
