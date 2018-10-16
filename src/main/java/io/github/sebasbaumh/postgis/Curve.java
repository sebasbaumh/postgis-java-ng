package io.github.sebasbaumh.postgis;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for simple curves like {@link LineString}s and complex classes like {@link CompoundCurve}s.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public abstract class Curve extends Geometry implements LineBasedGeometry
{
	private static final long serialVersionUID = 0x100;

	/**
	 * Constructor for subclasses.
	 * @param type has to be given by all subclasses
	 */
	protected Curve(int type)
	{
		super(type);
	}

	/**
	 * Closes this {@link Curve} if the last coordinate is not already the same as the first coordinate.
	 */
	public abstract void close();

}
