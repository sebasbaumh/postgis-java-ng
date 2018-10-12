/*
 * ByteGetter.java
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

/**
 * Allows reading bytes from a {@link String} in hex format.
 * @author Sebastian Baumhekel
 */
public class ByteGetter
{
	private final String rep;

	/**
	 * Constructs an instance.
	 * @param rep {@link String}
	 */
	public ByteGetter(String rep)
	{
		this.rep = rep;
	}

	private static int unhex(char c)
	{
		if (c >= '0' && c <= '9')
		{
			return (c - '0');
		}
		else if (c >= 'A' && c <= 'F')
		{
			return (c - 'A' + 10);
		}
		else if (c >= 'a' && c <= 'f')
		{
			return (c - 'a' + 10);
		}
		else
		{
			throw new IllegalArgumentException("No valid Hex char " + c);
		}
	}

	/**
	 * Gets a byte at the given index.
	 * @param index index
	 * @return byte
	 * @throws IndexOutOfBoundsException if the index is out of the range of the {@link String}
	 */
	public int get(int index)
	{
		index *= 2;
		return (unhex(rep.charAt(index)) << 4) + unhex(rep.charAt(index + 1));
	}

	/**
	 * Gets all bytes.
	 * @return bytes
	 */
	public byte[] getBytes()
	{
		byte[] data = new byte[rep.length() / 2];
		for (int i = 0; i < rep.length() / 2; i++)
		{
			data[i] = (byte) get(i);
		}
		return data;
	}

}
