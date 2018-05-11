package io.github.sebasbaumh.postgis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Base class for a polygon to allow similar handling of straight and circular polygons.
 * @author Sebastian Baumhekel
 * @param <T> type of the ring geometries
 */
public abstract class PolygonBase<T extends Geometry> extends Geometry implements Iterable<T>
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	private final ArrayList<T> rings = new ArrayList<T>();

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses
	 */
	protected PolygonBase(int type)
	{
		super(type);
	}

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses
	 * @param rings rings
	 */
	protected PolygonBase(int type, Collection<? extends T> rings)
	{
		this(type);
		this.rings.addAll(rings);
	}

	/**
	 * Adds a ring.
	 * @param ring ring
	 */
	public void addRing(T ring)
	{
		this.rings.add(ring);
	}

	/**
	 * Clears all rings.
	 */
	public void clearRings()
	{
		this.rings.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#equalsintern(io.github.sebasbaumh.postgis.Geometry)
	 */
	@Override
	protected boolean equalsintern(Geometry other)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getFirstPoint()
	 */
	@Override
	public Point getFirstPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getLastPoint()
	 */
	@Override
	public Point getLastPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getPoint(int)
	 */
	@Override
	public Point getPoint(int n)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the ring with the given index.
	 * @param idx index
	 * @return ring
	 */
	public T getRing(int idx)
	{
		return rings.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return this.rings.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#numPoints()
	 */
	@Override
	public int numPoints()
	{
		int c = 0;
		for (T ring : rings)
		{
			c += ring.numPoints();
		}
		return c;
	}

	/**
	 * Gets the number of rings.
	 * @return number of rings.
	 */
	public int numRings()
	{
		return rings.size();
	}
}
