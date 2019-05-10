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
 * @author Sebastian Baumhekel
 */
public class ValueGetter
{
	/**
	 * Int builder for big endian encoding.
	 */
	private static final IntBuilder INT_BUILDER_BIG_ENDIAN = (b1, b2, b3, b4) -> {
		return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
	};
	/**
	 * Int builder for little endian encoding.
	 */
	private static final IntBuilder INT_BUILDER_LITTLE_ENDIAN = (b1, b2, b3, b4) -> {
		return (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
	};
	/**
	 * Int builder for big endian encoding.
	 */
	private static final LongBuilder LONG_BUILDER_BIG_ENDIAN = (b1, b2, b3, b4, b5, b6, b7, b8) -> {
		return (b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32) + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8;
	};
	/**
	 * Int builder for little endian encoding.
	 */
	private static final LongBuilder LONG_BUILDER_LITTLE_ENDIAN = (b1, b2, b3, b4, b5, b6, b7, b8) -> {
		return (b8 << 56) + (b7 << 48) + (b6 << 40) + (b5 << 32) + (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
	};

	/**
	 * Current encoding (default is little endian encoding).
	 */
	private int endian = PostGisUtil.LITTLE_ENDIAN;
	/**
	 * Int builder (default is little endian encoding).
	 */
	private IntBuilder funcInt = INT_BUILDER_LITTLE_ENDIAN;
	/**
	 * Long builder (default is little endian encoding).
	 */
	private LongBuilder funcLong = LONG_BUILDER_LITTLE_ENDIAN;
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
	 * Gets a byte at the given index.
	 * @return byte
	 * @throws IndexOutOfBoundsException if the index is out of the range of the {@link String}
	 */
	private int getNextByte()
	{
		// get current position and advance it
		int index = position * 2;
		position++;
		return (PostGisUtil.toHexByte(value.charAt(index)) << 4) | PostGisUtil.toHexByte(value.charAt(index + 1));
	}

	/**
	 * Reads the encoding and adjusts the internal decoder if necessary.
	 * @throws IllegalArgumentException if the endian type is unknown
	 */
	public void readEncoding()
	{
		// get byte for endian check
		int newEndian = getNextByte();
		// only do something if encoding differs from the current setting
		if (newEndian != this.endian)
		{
			this.endian = newEndian;
			switch (newEndian)
			{
				case PostGisUtil.LITTLE_ENDIAN:
				{
					this.funcInt = INT_BUILDER_LITTLE_ENDIAN;
					this.funcLong = LONG_BUILDER_LITTLE_ENDIAN;
				}
					break;
				case PostGisUtil.BIG_ENDIAN:
				{
					this.funcInt = INT_BUILDER_BIG_ENDIAN;
					this.funcLong = LONG_BUILDER_BIG_ENDIAN;
				}
					break;
				default:
					throw new IllegalArgumentException("Unknown Endian type:" + endian);
			}
		}
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
