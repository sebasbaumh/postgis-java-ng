package io.github.sebasbaumh.postgis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	 * Little endian encoding.
	 */
	public static final byte LITTLE_ENDIAN = 1;

	// prevent instantiating this class
	@Deprecated
	private PostGisUtil()
	{
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
		if (Double.isNaN(a) && Double.isNaN(b))
		{
			return true;
		}
		return (a == b);
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
		// handle nulls
		if (la == null)
		{
			return (lb == null);
		}
		if (lb == null)
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
	 * Returns the number of elements in this collection. If this collection contains more than
	 * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
	 * @param col collection
	 * @return the number of elements in this collection
	 * @see Collection#size()
	 */
	public static <T> int size(Iterable<T> col)
	{
		// short cuts
		if (col instanceof Collection<?>)
		{
			return ((Collection<?>) col).size();
		}
		if (col instanceof Map<?, ?>)
		{
			return ((Map<?, ?>) col).size();
		}
		// walk through all elements
		Iterator<T> it = col.iterator();
		int i = 0;
		while (it.hasNext())
		{
			it.next();
			i++;
		}
		return i;
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

}
