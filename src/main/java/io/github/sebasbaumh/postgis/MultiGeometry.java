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
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for multi geometries.
 * @author Sebastian Baumhekel
 * @param <T> {@link Geometry} type
 */
@NonNullByDefault
public abstract class MultiGeometry<T extends Geometry> extends Geometry implements Iterable<T>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	/**
	 * Sub geometries.
	 */
	protected final ArrayList<T> subgeoms = new ArrayList<T>();

	/**
	 * Constructs an instance with the specified type.
	 * @param type int value corresponding to the geometry type
	 */
	protected MultiGeometry(int type)
	{
		super(type);
	}

	/**
	 * Constructs an instance with the specified type and geometries.
	 * @param type int value corresponding to the geometry type
	 * @param geoms geometries
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS")
	protected MultiGeometry(int type, Iterable<? extends T> geoms)
	{
		this(type);
		this.addAll(geoms);
	}

	/**
	 * Adds a geometry.
	 * @param geom geometry
	 */
	public void add(T geom)
	{
		subgeoms.add(geom);
	}

	/**
	 * Adds all given geometries.
	 * @param geoms geometries
	 */
	public void addAll(Iterable<? extends T> geoms)
	{
		for (T geom : geoms)
		{
			subgeoms.add(geom);
		}
	}

	@Override
	public boolean checkConsistency()
	{
		if (!super.checkConsistency() || subgeoms.isEmpty())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(subgeoms);
	}

//	@Override
//	public boolean equals(@Nullable Object other)
//	{
//		// check type and parent
//		if ((other instanceof MultiGeometry<?> cother) && super.equals(other))
//		{
//			return PostGisUtil.equalsIterable(this.subgeoms, cother.subgeoms);
//		}
//		return false;
//	}

	@Override
	public boolean equals(@Nullable Object other) {
		// check type and parent
		if (other instanceof MultiGeometry) {
			MultiGeometry<?> cother = (MultiGeometry<?>) other;  // 手动转换类型
			if (super.equals(other)) {
				return PostGisUtil.equalsIterable(this.subgeoms, cother.subgeoms);
			}
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getCoordinates()
	 */
	@Override
	public Iterable<Point> getCoordinates()
	{
		ArrayList<Point> l = new ArrayList<Point>();
		for (T geom : subgeoms)
		{
			for (Point p : geom.getCoordinates())
			{
				l.add(p);
			}
		}
		return l;
	}

	/**
	 * Gets all geometries.
	 * @return geometries
	 */
	public Collection<T> getGeometries()
	{
		return subgeoms;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getNumberOfCoordinates()
	 */
	@Override
	public int getNumberOfCoordinates()
	{
		int n = 0;
		for (T geom : subgeoms)
		{
			n += geom.getNumberOfCoordinates();
		}
		return n;
	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + subgeoms.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#hasMeasure()
	 */
	@Override
	public boolean hasMeasure()
	{
		for (T geom : subgeoms)
		{
			if (geom.hasMeasure())
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#is3d()
	 */
	@Override
	public boolean is3d()
	{
		for (T geom : subgeoms)
		{
			if (geom.is3d())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks, if there are no sub-geometries.
	 * @return true on success, else false
	 */
	@Override
	public boolean isEmpty()
	{
		return subgeoms.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return subgeoms.iterator();
	}

	@Override
	public void setSrid(int srid)
	{
		super.setSrid(srid);
		for (T geom : subgeoms)
		{
			geom.setSrid(srid);
		}
	}

	/**
	 * Gets the number of contained geometries.
	 * @return number of contained geometries
	 */
	public int size()
	{
		return this.subgeoms.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " [" + this.size() + " geometries]";
	}

}
