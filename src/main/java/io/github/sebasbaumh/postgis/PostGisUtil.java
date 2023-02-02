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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for helper functions.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public final class PostGisUtil
{
	/**
	 * Big endian encoding.
	 */
	public static final byte BIG_ENDIAN = 0;
	/**
	 * Epsilon/tolerance for comparing double values.
	 */
	private static double EPSILON = 1e-15;
	/**
	 * Characters for converting data to hex strings.
	 */
	public static final char[] HEX_CHAR = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };
	/**
	 * Little endian encoding.
	 */
	public static final byte LITTLE_ENDIAN = 1;

	// prevent instantiating this class
	@Deprecated
	private PostGisUtil()
	{
	}

	/**
	 * Calculates the area of the outer ring of the given polygon (signed).
	 * @param points points
	 * @return area (signed depending on direction)
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("UMTP_UNBOUND_METHOD_TEMPLATE_PARAMETER")
	public static double calcAreaSigned(Iterable<Point> points)
	{
		Iterator<Point> it = points.iterator();
		if (it.hasNext())
		{
			Point p1 = it.next();
			if (it.hasNext())
			{
				Point pFirst = p1;
				double area = 0;
				boolean b = true;
				do
				{
					Point p2;
					if (it.hasNext())
					{
						p2 = it.next();
					}
					else
					{
						p2 = pFirst;
						b = false;
					}
					area += ((p1.getX() + p2.getX()) * (p2.getY() - p1.getY()));
					p1 = p2;
				}
				while (b);
				return area / 2;
			}
		}
		return 0;
	}

	/**
	 * Do some internal consistency checks on the given geometries. Currently, all Geometries must have a valid
	 * dimension (2 or 3) and a valid type. Composed geometries must have all equal SRID, dimensionality and measures,
	 * as well as that they do not contain NULL or inconsistent subgeometries. BinaryParser and WKTParser should only
	 * generate consistent geometries. BinaryWriter may produce invalid results on inconsistent geometries.
	 * @param geoms geometries
	 * @return true if all checks are passed.
	 */
	public static <T extends Geometry> boolean checkConsistency(Iterable<T> geoms)
	{
		Iterator<T> it = geoms.iterator();
		if (it.hasNext())
		{
			// get first geometry and check it
			T gFirst = it.next();
			if (!gFirst.checkConsistency())
			{
				return false;
			}
			boolean bIs3d = gFirst.is3d();
			boolean bHasMeasure = gFirst.hasMeasure();
			int iSrid = gFirst.getSrid();
			// now compare to the rest
			while (it.hasNext())
			{
				T subGeom = it.next();
				if (!subGeom.checkConsistency() || (bIs3d != subGeom.is3d()) || (bHasMeasure != subGeom.hasMeasure())
						|| (iSrid != subGeom.getSrid()))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compares two double values respecting {@link Double#NaN} values.
	 * @param a {@link Double}
	 * @param b {@link Double}
	 * @return true if they are equal, else false
	 */
	public static boolean equalsDouble(double a, double b)
	{
		// check if both values are NaN
		if (Double.isNaN(a) && Double.isNaN(b))
		{
			return true;
		}
		// check which value is larger to avoid a call to Math.abs, use a tolerance to avoid rounding errors
		// if any of the values is NaN, the comparision below will return false anyway
		if (a < b)
		{
			return (b - a) <= EPSILON;
		}
		else
		{
			return (a - b) <= EPSILON;
		}
	}

	/**
	 * Checks, if the given {@link Iterable}s contain the same elements (in the same order).
	 * @param la {@link Iterable} (can be null)
	 * @param lb {@link Iterable} (can be null)
	 * @return true on success, else false
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static <T, U> boolean equalsIterable(@Nullable Iterable<T> la, @Nullable Iterable<U> lb)
	{
		// check same instance
		if (la == lb)
		{
			return true;
		}
		// iterables are different instances, so none of them should be null to proceed
		if ((la == null) || (lb == null))
		{
			return false;
		}
		// walk through items
		Iterator<T> it = la.iterator();
		Iterator<U> it2 = lb.iterator();
		while (it.hasNext() && it2.hasNext())
		{
			// check items
			if (!Objects.equals(it.next(), it2.next()))
			{
				return false;
			}
		}
		// make sure there are no more items
		return !it.hasNext() && !it2.hasNext();
	}

	/**
	 * Returns the first or a default element of the given collection.
	 * @param elements collection
	 * @return first element if it exists, else default element
	 */
	@Nullable
	public static <T> T firstOrDefault(Iterable<T> elements)
	{
		Iterator<T> i = elements.iterator();
		if (i.hasNext())
		{
			return i.next();
		}
		return null;
	}

	/**
	 * Gets the last element of the given {@link Iterable}.
	 * @param elements elements
	 * @return last element on success, else null
	 */
	@Nullable
	public static <T> T lastOrDefault(Iterable<T> elements)
	{
		// get element from list directly without traversing list
		if (elements instanceof List)
		{
			List<T> l = (List<T>) elements;
			int size = l.size();
			if (size > 0)
			{
				return l.get(size - 1);
			}
			return null;
		}
		// just walk through all the elements
		T last = null;
		for (T e : elements)
		{
			last = e;
		}
		return last;
	}

	/**
	 * Removes brackets from the given {@link String}.
	 * @param s {@link String}
	 * @return {@link String} without brackets
	 */
	public static String removeBrackets(String s)
	{
		if (s.length() > 1)
		{
			int iStart = 0;
			int iEnd = s.length() - 1;
			if (s.charAt(0) == '(')
			{
				iStart++;
			}
			if (s.charAt(iEnd) == ')')
			{
				iEnd--;
			}
			return s.substring(iStart, iEnd + 1);
		}
		return s;
	}

	/**
	 * Splits a string like {@link String#split(String)} without any support for regular expressions, but faster.
	 * @param value {@link String} to split.
	 * @param separator separator
	 * @return the array of strings computed by splitting this string
	 */
	public static List<String> split(String value, char separator)
	{
		int off = 0;
		int next;
		ArrayList<String> list = new ArrayList<String>();
		while ((next = value.indexOf(separator, off)) != -1)
		{
			list.add(value.substring(off, next));
			off = next + 1;
		}
		// Add remaining segment
		list.add(value.substring(off, value.length()));
		return list;
	}

	/**
	 * Converts the given hexadecimal character to its byte representation. i.e. 'A'-&gt;10
	 * @param c character
	 * @return byte value
	 * @throws IllegalArgumentException if character is not '0'-'9', 'a'-'f' or 'A'-'F'
	 */
	public static int toHexByte(char c)
	{
		// '0'
		if (c >= 0x30)
		{
			// '9'
			if (c <= 0x39)
			{
				return c - 0x30;
			}
			// 'A'
			else if (c >= 0x41)
			{
				// 'F'
				if (c <= 0x46)
				{
					return c - 0x37;
				} // 'a' - 'f'
				else if ((c >= 0x61) && (c <= 0x66))
				{
					return c - 0x57;
				}
			}
		}
		throw new IllegalArgumentException("character is no hexadecimal digit: " + c);
	}

	/**
	 * Converts the given string in hexadecimal format to the corresponding bytes.
	 * @param hex {@link String} in hex
	 * @return byte data
	 */
	public static byte[] toHexBytes(String hex)
	{
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0; i < b.length; i++)
		{
			b[i] = (byte) ((toHexByte(hex.charAt(i * 2)) << 4) | (toHexByte(hex.charAt(i * 2 + 1))));
		}
		return b;
	}

	/**
	 * Converts the byte data to a hexadecimal string.
	 * @param data byte data
	 * @return hexadecimal {@link String} (lower case)
	 */
	public static String toHexString(byte[] data)
	{
		char[] sb = new char[data.length * 2];
		for (int i = 0; i < data.length; i++)
		{
			sb[i * 2] = HEX_CHAR[(data[i] & 0xF0) >> 4];
			sb[i * 2 + 1] = HEX_CHAR[(data[i] & 0x0F)];
		}
		return new String(sb);
	}

}
