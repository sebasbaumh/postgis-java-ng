package io.github.sebasbaumh.postgis;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The MULTISURFACE is a collection of surfaces, which can be (linear) polygons or curve polygons.
 * @author Sebastian Baumhekel
 */
@NonNullByDefault
public class MultiSurface extends MultiGeometry<PolygonBase<?>>
{
	private static final long serialVersionUID = 0x100;
	/**
	 * The OGIS geometry type number for multi surfaces, which can be (linear) polygons or curve polygons.
	 */
	public static final int TYPE = 12;

	/**
	 * Constructs an instance.
	 */
	public MultiSurface()
	{
		super(TYPE);
	}

	/**
	 * Constructs an instance.
	 * @param lines lines
	 */
	public <T extends PolygonBase<?>> MultiSurface(Iterable<T> lines)
	{
		super(TYPE);
		for (T geom : lines)
		{
			add(geom);
		}
	}

}
