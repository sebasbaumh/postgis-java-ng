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
public abstract class ValueGetter
{
	protected final ByteGetter data;
	private final byte endian;
	private int position;

	/**
	 * Constructs an instance.
	 * @param data {@link ByteGetter}
	 * @param endian endianess
	 */
	public ValueGetter(ByteGetter data, byte endian)
	{
		this.data = data;
		this.endian = endian;
	}

	/**
	 * Get the appropriate {@link ValueGetter} for the given endianness.
	 * @param bytes {@link ByteGetter}
	 * @return {@link ValueGetter}
	 * @throws IllegalArgumentException if the endian type is unknown
	 */
	public static ValueGetter getValueGetterForEndian(ByteGetter bytes)
	{
		switch (bytes.get(0))
		{
			case PostGisUtil.BIG_ENDIAN:
				return new XDR(bytes);
			case PostGisUtil.LITTLE_ENDIAN:
				return new NDR(bytes);
			default:
				throw new IllegalArgumentException("Unknown Endian type:" + bytes.get(0));
		}
	}

	/**
	 * Get a byte, should be equal for all endians
	 * @return the byte value
	 */
	public byte getByte()
	{
		return (byte) data.get(position++);
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
	 * @return interger
	 */
	public int getInt()
	{
		int res = getInt(position);
		position += 4;
		return res;
	}

	/**
	 * Get a 32-Bit integer
	 * @param index the index to get the value from
	 * @return the int value
	 */
	protected abstract int getInt(int index);

	/**
	 * Get a long.
	 * @return long
	 */
	public long getLong()
	{
		long res = getLong(position);
		position += 8;
		return res;
	}

	/**
	 * Get a long value. This is not needed directly, but as a nice side-effect from GetDouble.
	 * @param index the index to get the value from
	 * @return the long value
	 */
	protected abstract long getLong(int index);

	/**
	 * {@link ValueGetter} for little endian data.
	 */
	private static class NDR extends ValueGetter
	{
		/**
		 * Constructs an instance.
		 * @param data {@link ByteGetter}
		 */
		public NDR(ByteGetter data)
		{
			super(data, PostGisUtil.LITTLE_ENDIAN);
		}

		@Override
		protected int getInt(int index)
		{
			return (data.get(index + 3) << 24) + (data.get(index + 2) << 16) + (data.get(index + 1) << 8)
					+ data.get(index);
		}

		@Override
		protected long getLong(int index)
		{
			return ((long) data.get(index + 7) << 56) + ((long) data.get(index + 6) << 48)
					+ ((long) data.get(index + 5) << 40) + ((long) data.get(index + 4) << 32)
					+ ((long) data.get(index + 3) << 24) + ((long) data.get(index + 2) << 16)
					+ ((long) data.get(index + 1) << 8) + ((long) data.get(index) << 0);
		}
	}

	/**
	 * {@link ValueGetter} for big endian data.
	 */
	private static class XDR extends ValueGetter
	{
		/**
		 * Constructs an instance.
		 * @param data {@link ByteGetter}
		 */
		public XDR(ByteGetter data)
		{
			super(data, PostGisUtil.BIG_ENDIAN);
		}

		@Override
		protected int getInt(int index)
		{
			return (data.get(index) << 24) + (data.get(index + 1) << 16) + (data.get(index + 2) << 8)
					+ data.get(index + 3);
		}

		@Override
		protected long getLong(int index)
		{
			return ((long) data.get(index) << 56) + ((long) data.get(index + 1) << 48)
					+ ((long) data.get(index + 2) << 40) + ((long) data.get(index + 3) << 32)
					+ ((long) data.get(index + 4) << 24) + ((long) data.get(index + 5) << 16)
					+ ((long) data.get(index + 6) << 8) + ((long) data.get(index + 7) << 0);
		}
	}
}
