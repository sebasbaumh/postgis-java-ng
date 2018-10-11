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
		if (!super.checkConsistency())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(subgeoms);
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		// check parent
		if (super.equals(other) && (other instanceof MultiGeometry<?>))
		{
			MultiGeometry<?> cother = (MultiGeometry<?>) other;
			if (cother.subgeoms.isEmpty() && subgeoms.isEmpty())
			{
				return true;
			}
			else if (cother.subgeoms.size() != subgeoms.size())
			{
				return false;
			}
			else
			{
				for (int i = 0; i < subgeoms.size(); i++)
				{
					if (!cother.subgeoms.get(i).equals(this.subgeoms.get(i)))
					{
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Point getFirstPoint()
	{
		return subgeoms.get(0).getFirstPoint();
	}

	/**
	 * Gets all geometries.
	 * @return geometries
	 */
	public Collection<T> getGeometries()
	{
		return subgeoms;
	}

	/**
	 * Gets the sub geometry at the given index.
	 * @param index index
	 * @return sub geometry
	 */
	public T getGeometry(int index)
	{
		return subgeoms.get(index);
	}

	@Override
	public Point getLastPoint()
	{
		return subgeoms.get(subgeoms.size() - 1).getLastPoint();
	}

	@Override
	public Point getPoint(int n)
	{
		if (n < 0)
		{
			throw new ArrayIndexOutOfBoundsException("Negative index not allowed");
		}
		else if (subgeoms.isEmpty())
		{
			throw new ArrayIndexOutOfBoundsException("Empty Geometry has no Points!");
		}
		else
		{
			for (Geometry current : subgeoms)
			{
				int np = current.numPoints();
				if (n < np)
				{
					return current.getPoint(n);
				}
				else
				{
					n -= np;
				}
			}
			throw new ArrayIndexOutOfBoundsException("Index too large!");
		}
	}

	@Override
	public int hashCode()
	{
		return subgeoms.hashCode();
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
	public int numPoints()
	{
		int result = 0;
		for (T geom : subgeoms)
		{
			result += geom.numPoints();
		}
		return result;
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
}
