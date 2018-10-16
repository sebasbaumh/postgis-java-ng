package io.github.sebasbaumh.postgis;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for a polygon to allow similar handling of straight and circular polygons.
 * @author Sebastian Baumhekel
 * @param <T> type of the ring geometries
 */
@NonNullByDefault
public abstract class PolygonBase<T extends Curve> extends Geometry implements Iterable<T>, LineBasedGeometry
{
	/* JDK 1.5 Serialization */
	private static final long serialVersionUID = 0x100;

	private T lsOuterRing;
	private final ArrayList<T> rings = new ArrayList<T>();

	/**
	 * Constructor for subclasses.
	 * @param clazzRing class of the ring
	 * @param type has to be given by all subclasses
	 */
	protected <U extends T> PolygonBase(int type, Class<U> clazzRing)
	{
		super(type);
		this.lsOuterRing = createRing(clazzRing);
	}

	/**
	 * Constructor for subclasses.
	 * @param clazzRing class of the ring
	 * @param type has to be given by all subclasses
	 * @param rings rings
	 */
	protected <U extends T, V extends T> PolygonBase(int type, Class<U> clazzRing, Iterable<V> rings)
	{
		super(type);
		Iterator<V> it = rings.iterator();
		// first the outer ring
		if (it.hasNext())
		{
			this.lsOuterRing = it.next();
			// inner rings
			while (it.hasNext())
			{
				this.rings.add(it.next());
			}
		}
		else
		{
			this.lsOuterRing = createRing(clazzRing);
		}
	}

	/**
	 * Adds a ring.
	 * @param ring ring
	 */
	public void addRing(T ring)
	{
		// ensure ring is closed
		if (!ring.isClosed())
		{
			ring.close();
		}
		this.rings.add(ring);
	}

	@Override
	public boolean checkConsistency()
	{
		if (!super.checkConsistency())
		{
			return false;
		}
		return PostGisUtil.checkConsistency(rings);
	}

	/**
	 * Clears all rings.
	 */
	public void clearRings()
	{
		this.rings.clear();
	}

	/**
	 * Creates a new empty ring.
	 * @param clazzRing class of the ring
	 * @return ring
	 * @throws IllegalArgumentException if the class could not be created
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
	private <U extends T> T createRing(Class<U> clazzRing)
	{
		try
		{
			return clazzRing.getDeclaredConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e)
		{
			throw new IllegalArgumentException("unable to create empty ring", e);
		}
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		// check parent
		if (super.equals(other) && (other instanceof PolygonBase<?>))
		{
			PolygonBase<?> poly = (PolygonBase<?>) other;
			return PostGisUtil.equalsIterable(this.rings, poly.rings);
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
		return lsOuterRing.getCoordinates();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getEndPoint()
	 */
	@Override
	public Point getEndPoint()
	{
		return lsOuterRing.getEndPoint();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#getNumberOfCoordinates()
	 */
	@Override
	public int getNumberOfCoordinates()
	{
		return lsOuterRing.getNumberOfCoordinates();
	}

	/**
	 * Gets the number of rings.
	 * @return number of rings.
	 */
	public int getNumberOfRings()
	{
		return rings.size();
	}

	/**
	 * Gets the outer ring/boundary of the polygon.
	 * @return outer ring
	 */
	public T getOuterRing()
	{
		return this.lsOuterRing;
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
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#getStartPoint()
	 */
	@Override
	public Point getStartPoint()
	{
		return lsOuterRing.getStartPoint();
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#hasMeasure()
	 */
	@Override
	public boolean hasMeasure()
	{
		for (T geom : rings)
		{
			if (geom.hasMeasure())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets all inner rings.
	 * @return inner rings
	 */
	public Iterable<T> innerRings()
	{
		return this.rings;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.Geometry#is3d()
	 */
	@Override
	public boolean is3d()
	{
		for (T geom : rings)
		{
			if (geom.is3d())
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see io.github.sebasbaumh.postgis.LineBasedGeometry#isClosed()
	 */
	@Override
	public boolean isClosed()
	{
		return this.lsOuterRing.isClosed();
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
	 * @see io.github.sebasbaumh.postgis.LineBasedGeom#length()
	 */
	@Override
	public double length()
	{
		return this.lsOuterRing.length();
	}

}
