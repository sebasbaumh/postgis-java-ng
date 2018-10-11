package io.github.sebasbaumh.postgis;

import java.util.Iterator;
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
	 * Parse a SRID value, anything {@code <= 0} is unknown
	 * @param srid the SRID to parse
	 * @return parsed SRID value
	 */
	public static int parseSRID(int srid)
	{
		if (srid < 0)
		{
			srid = 0;
		}
		return srid;
	}

}
