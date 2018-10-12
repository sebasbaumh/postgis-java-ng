/*
 * BinaryParser.java
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

import java.util.ArrayList;
import java.util.Collection;

import io.github.sebasbaumh.postgis.CircularString;
import io.github.sebasbaumh.postgis.CompoundCurve;
import io.github.sebasbaumh.postgis.CurvePolygon;
import io.github.sebasbaumh.postgis.Geometry;
import io.github.sebasbaumh.postgis.GeometryCollection;
import io.github.sebasbaumh.postgis.LineString;
import io.github.sebasbaumh.postgis.LinearRing;
import io.github.sebasbaumh.postgis.MultiCurve;
import io.github.sebasbaumh.postgis.MultiLineString;
import io.github.sebasbaumh.postgis.MultiPoint;
import io.github.sebasbaumh.postgis.MultiPolygon;
import io.github.sebasbaumh.postgis.MultiSurface;
import io.github.sebasbaumh.postgis.Point;
import io.github.sebasbaumh.postgis.Polygon;
import io.github.sebasbaumh.postgis.PolygonBase;
import io.github.sebasbaumh.postgis.PostGisUtil;

/**
 * Parse binary representation of geometries. It should be easy to add char[] and CharSequence ByteGetter instances,
 * although the latter one is not compatible with older jdks. I did not implement real unsigned 32-bit integers or
 * emulate them with long, as both java Arrays and Strings currently can have only 2^31-1 elements (bytes), so we cannot
 * even get or build Geometries with more than approx. 2^28 coordinates (8 bytes each).
 * @author {@literal Markus Schaber <markus.schaber@logix-tt.com>}
 */
public class BinaryParser
{

	/**
	 * Parse a hex encoded geometry
	 * @param value String containing the data to be parsed
	 * @return resulting geometry for the parsed data
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type or the encoding type is unknown
	 */
	public static Geometry parse(String value)
	{
		return parseGeometry(ValueGetter.getValueGetterForEndian(new ByteGetter(value)));
	}

	private static CircularString parseCircularString(ValueGetter data, boolean haveZ, boolean haveM)
	{
		return new CircularString(parsePoints(data, haveZ, haveM));
	}

	private static GeometryCollection parseCollection(ValueGetter data)
	{
		return new GeometryCollection(parseGeometries(Geometry.class, data));
	}

	private static CompoundCurve parseCompoundCurve(ValueGetter data)
	{
		return new CompoundCurve(parseGeometries(LineString.class, data));
	}

	private static CurvePolygon parseCurvePolygon(ValueGetter data)
	{
		return new CurvePolygon(parseGeometries(LineString.class, data));
	}

	/**
	 * Parse multiple geometries into a {@link Collection}. The number of geometries is read upfront from the
	 * {@link ValueGetter}.
	 * @param clazz {@link Class} of the geometries
	 * @param data {@link ValueGetter}
	 * @return {@link Collection} of geometries
	 * @throws IllegalArgumentException if a contained geometry is of the wrong type
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Geometry> Collection<T> parseGeometries(Class<T> clazz, ValueGetter data)
	{
		// get number of geometries to parse
		int count = data.getInt();
		ArrayList<T> l = new ArrayList<T>(count);
		// parse geometries
		for (int i = 0; i < count; i++)
		{
			Geometry geom = parseGeometry(data);
			// check if the geometry is of the correct type
			if (clazz.isInstance(geom))
			{
				l.add((T) geom);
			}
			else
			{
				throw new IllegalArgumentException(
						"expected: " + clazz.getCanonicalName() + " got: " + geom.getClass().getCanonicalName());
			}
		}
		return l;
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param data ValueGetter with the data to be parsed
	 * @return the parsed geometry
	 */
	private static Geometry parseGeometry(ValueGetter data)
	{
		byte endian = data.getByte(); // skip and test endian flag
		if (endian != data.getEndian())
		{
			throw new IllegalArgumentException("Endian inconsistency!");
		}

		int typeword = data.getInt();
		int geometryType = typeword & 0x1FFFFFFF; // cut off high flag bits

		boolean haveZ = (typeword & 0x80000000) != 0;
		boolean haveM = (typeword & 0x40000000) != 0;
		boolean haveS = (typeword & 0x20000000) != 0;

		int srid;
		if (haveS)
		{
			srid = PostGisUtil.parseSRID(data.getInt());
		}
		else
		{
			srid = Geometry.UNKNOWN_SRID;
		}
		// parse geometry according to type
		Geometry result;
		switch (geometryType)
		{
			case Point.TYPE:
				result = parsePoint(data, haveZ, haveM);
				break;
			case LineString.TYPE:
				result = parseLineString(data, haveZ, haveM);
				break;
			case CircularString.TYPE:
				result = parseCircularString(data, haveZ, haveM);
				break;
			case CompoundCurve.TYPE:
				result = parseCompoundCurve(data);
				break;
			case Polygon.TYPE:
				result = parsePolygon(data, haveZ, haveM);
				break;
			case CurvePolygon.TYPE:
				result = parseCurvePolygon(data);
				break;
			case MultiPoint.TYPE:
				result = parseMultiPoint(data);
				break;
			case MultiLineString.TYPE:
				result = parseMultiLineString(data);
				break;
			case MultiCurve.TYPE:
				result = parseMultiCurve(data);
				break;
			case MultiPolygon.TYPE:
				result = parseMultiPolygon(data);
				break;
			case MultiSurface.TYPE:
				result = parseMultiSurface(data);
				break;
			case GeometryCollection.TYPE:
				result = parseCollection(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
		}
		// set SRID and return the geometry
		result.setSrid(srid);
		return result;
	}

	private static LinearRing parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM)
	{
		return new LinearRing(parsePoints(data, haveZ, haveM));
	}

	private static LineString parseLineString(ValueGetter data, boolean haveZ, boolean haveM)
	{
		return new LineString(parsePoints(data, haveZ, haveM));
	}

	private static MultiCurve parseMultiCurve(ValueGetter data)
	{
		return new MultiCurve(parseGeometries(LineString.class, data));
	}

	private static MultiLineString parseMultiLineString(ValueGetter data)
	{
		return new MultiLineString(parseGeometries(LineString.class, data));
	}

	private static MultiPoint parseMultiPoint(ValueGetter data)
	{
		return new MultiPoint(parseGeometries(Point.class, data));
	}

	private static MultiPolygon parseMultiPolygon(ValueGetter data)
	{
		return new MultiPolygon(parseGeometries(Polygon.class, data));
	}

	private static MultiSurface parseMultiSurface(ValueGetter data)
	{
		return new MultiSurface(parseGeometries(PolygonBase.class, data));
	}

	/**
	 * Parse a single point.
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Point}
	 */
	private static Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM)
	{
		double X = data.getDouble();
		double Y = data.getDouble();
		Point result;
		// parse z?
		if (haveZ)
		{
			result = new Point(X, Y, data.getDouble());
		}
		else
		{
			result = new Point(X, Y);
		}
		// parse measure?
		if (haveM)
		{
			result.setM(data.getDouble());
		}
		return result;
	}

	/**
	 * Parse an Array of "slim" Points (without endianness and type, part of LinearRing and Linestring, but not
	 * MultiPoint!
	 * @param data {@link ValueGetter}
	 * @param haveZ parse z value?
	 * @param haveM parse measure value?
	 * @return {@link Collection} of {@link Point}s
	 */
	private static Collection<Point> parsePoints(ValueGetter data, boolean haveZ, boolean haveM)
	{
		int count = data.getInt();
		ArrayList<Point> l = new ArrayList<Point>(count);
		for (int i = 0; i < count; i++)
		{
			l.add(parsePoint(data, haveZ, haveM));
		}
		return l;
	}

	private static Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM)
	{
		int count = data.getInt();
		ArrayList<LinearRing> rings = new ArrayList<LinearRing>(count);
		for (int i = 0; i < count; i++)
		{
			rings.add(parseLinearRing(data, haveZ, haveM));
		}
		return new Polygon(rings);
	}
}
