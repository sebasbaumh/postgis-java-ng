/*
 * ValueGetter.java
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
 * Allows reading values.
 * @author sbaumhekel
 */
public class ValueGetter
{
	private final byte endian;
	private final IntBuilder funcInt;
	private final LongBuilder funcLong;
	private int position;
	private final String value;

	/**
	 * Constructs an instance.
	 * @param value value as hex string
	 * @throws IllegalArgumentException if the endian type is unknown
	 */
	public ValueGetter(String value)
	{
		this.value = value;
		this.endian = (byte) getByteAt(value, 0);
		switch (endian)
		{
			case PostGisUtil.LITTLE_ENDIAN:
			{
				this.funcInt = (b1, b2, b3, b4) -> {
					return (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
				};
				this.funcLong = (b1, b2, b3, b4, b5, b6, b7, b8) -> {
					return (b8 << 56) + (b7 << 48) + (b6 << 40) + (b5 << 32) + (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
				};
			}
				break;
			case PostGisUtil.BIG_ENDIAN:
			{
				this.funcInt = (b1, b2, b3, b4) -> {
					return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
				};
				this.funcLong = (b1, b2, b3, b4, b5, b6, b7, b8) -> {
					return (b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32) + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8;
				};
			}
				break;
			default:
				throw new IllegalArgumentException("Unknown Endian type:" + endian);
		}
	}

	/**
	 * Gets a byte at the given index.
	 * @param rep {@link String}
	 * @param index index
	 * @return byte
	 * @throws IndexOutOfBoundsException if the index is out of the range of the {@link String}
	 */
	private static int getByteAt(String rep, int index)
	{
		index *= 2;
		return (PostGisUtil.toHexByte(rep.charAt(index)) << 4) | PostGisUtil.toHexByte(rep.charAt(index + 1));
	}

	/**
	 * Get a byte, should be equal for all endians
	 * @return the byte value
	 */
	public byte getByte()
	{
		return (byte) getNextByte();
	}

	/**
	 * Get a double.
	 * @return the double value
	 */
	public double getDouble()
	{
		return Double.longBitsToDouble(getLong());
	}

	/**
	 * Gets the endian encoding.
	 * @return endian encoding
	 */
	public int getEndian()
	{
		return this.endian;
	}

	/**
	 * Get an integer.
	 * @return integer
	 */
	public int getInt()
	{
		return funcInt.getInt(getNextByte(), getNextByte(), getNextByte(), getNextByte());
	}

	/**
	 * Get a long.
	 * @return long
	 */
	public long getLong()
	{
		return funcLong.getLong(getNextByte(), getNextByte(), getNextByte(), getNextByte(), getNextByte(),
				getNextByte(), getNextByte(), getNextByte());
	}

	/**
	 * Get a byte as an int.
	 * @return the byte value
	 */
	private int getNextByte()
	{
		return getByteAt(value, position++);
	}

	/**
	 * Builder for an int from a byte sequence.
	 */
	@FunctionalInterface
	private interface IntBuilder
	{
		/**
		 * Get an integer.
		 * @param b1 byte 1
		 * @param b2 byte 2
		 * @param b3 byte 3
		 * @param b4 byte 4
		 * @return integer
		 */
		int getInt(int b1, int b2, int b3, int b4);
	}

	/**
	 * Builder for a long from a byte sequence.
	 */
	@FunctionalInterface
	private interface LongBuilder
	{
		/**
		 * Get a long.
		 * @param b1 byte 1
		 * @param b2 byte 2
		 * @param b3 byte 3
		 * @param b4 byte 4
		 * @param b5 byte 5
		 * @param b6 byte 6
		 * @param b7 byte 7
		 * @param b8 byte 8
		 * @return long
		 */
		long getLong(long b1, long b2, long b3, long b4, long b5, long b6, long b7, long b8);
	}

}
