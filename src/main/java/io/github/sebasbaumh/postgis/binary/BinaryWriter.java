/*
 * BinaryWriter.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - Binary Writer
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

import java.util.Collection;
import java.util.Iterator;

import io.github.sebasbaumh.postgis.CircularString;
import io.github.sebasbaumh.postgis.CurvePolygon;
import io.github.sebasbaumh.postgis.Geometry;
import io.github.sebasbaumh.postgis.GeometryCollection;
import io.github.sebasbaumh.postgis.LineString;
import io.github.sebasbaumh.postgis.LinearRing;
import io.github.sebasbaumh.postgis.MultiLineString;
import io.github.sebasbaumh.postgis.MultiPoint;
import io.github.sebasbaumh.postgis.MultiPolygon;
import io.github.sebasbaumh.postgis.Point;
import io.github.sebasbaumh.postgis.Polygon;

/**
 * Create binary representation of geometries. Currently, only text rep (hexed) implementation is tested. It should be
 * easy to add char[] and CharSequence ByteGetter instances, although the latter one is not compatible with older jdks.
 * I did not implement real unsigned 32-bit integers or emulate them with long, as both java Arrays and Strings
 * currently can have only 2^31-1 elements (bytes), so we cannot even get or build Geometries with more than approx.
 * 2^28 coordinates (8 bytes each).
 * @author markus.schaber@logi-track.com
 */
public class BinaryWriter
{

	/**
	 * Get the appropriate ValueGetter for my endianness
	 * @param bytes The ByteSetter to use
	 * @param endian the endian for the ValueSetter to use
	 * @return the ValueGetter
	 */
	public static ValueSetter valueSetterForEndian(ByteSetter bytes, byte endian)
	{
		if (endian == ValueSetter.XDR.NUMBER)
		{ // XDR
			return new ValueSetter.XDR(bytes);
		}
		else if (endian == ValueSetter.NDR.NUMBER)
		{
			return new ValueSetter.NDR(bytes);
		}
		else
		{
			throw new IllegalArgumentException("Unknown Endian type:" + endian);
		}
	}

	/**
	 * Write a hex encoded geometry The geometry you put in must be consistent, geom.checkConsistency() must return
	 * true. If not, the result may be invalid WKB.
	 * @see Geometry#checkConsistency() the consistency checker
	 * @param geom the geometry to be written
	 * @param REP endianness to write the bytes with
	 * @return String containing the hex encoded geometry
	 */
	public static String writeHexed(Geometry geom, byte REP)
	{
		ByteSetter bytes = new ByteSetter();
		writeGeometry(geom, valueSetterForEndian(bytes, REP));
		return bytes.toString();
	}

	/**
	 * Write a hex encoded geometry The geometry you put in must be consistent, geom.checkConsistency() must return
	 * true. If not, the result may be invalid WKB.
	 * @see Geometry#checkConsistency() the consistency checker
	 * @param geom the geometry to be written
	 * @return String containing the hex encoded geometry
	 */
	public static String writeHexed(Geometry geom)
	{
		return writeHexed(geom, ValueSetter.NDR.NUMBER);
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param geom the geometry to write
	 * @param dest the value setting to be used for writing
	 */
	protected static void writeGeometry(Geometry geom, ValueSetter dest)
	{
		// write endian flag
		dest.setByte(dest.endian);

		// write typeword
		int typeword = geom.type;
		if (geom.dimension == 3)
		{
			typeword |= 0x80000000;
		}
		if (geom.haveMeasure)
		{
			typeword |= 0x40000000;
		}
		if (geom.srid != Geometry.UNKNOWN_SRID)
		{
			typeword |= 0x20000000;
		}

		dest.setInt(typeword);

		if (geom.srid != Geometry.UNKNOWN_SRID)
		{
			dest.setInt(geom.srid);
		}

		switch (geom.type)
		{
			case Geometry.POINT:
				writePoint((Point) geom, dest);
				break;
			case Geometry.LINESTRING:
				writeLineString((LineString) geom, dest);
				break;
			case Geometry.POLYGON:
				writePolygon((Polygon) geom, dest);
				break;
			case Geometry.MULTIPOINT:
				writeMultiPoint((MultiPoint) geom, dest);
				break;
			case Geometry.MULTILINESTRING:
				writeMultiLineString((MultiLineString) geom, dest);
				break;
			case Geometry.MULTIPOLYGON:
				writeMultiPolygon((MultiPolygon) geom, dest);
				break;
			case Geometry.GEOMETRYCOLLECTION:
				writeCollection((GeometryCollection) geom, dest);
				break;
			case Geometry.CIRCULARSTRING:
				writeOnlyPoints((CircularString) geom, dest);
				break;
			case Geometry.CURVEPOLYGON:
				writeCurvePolygon((CurvePolygon) geom, dest);
				break;
			// FIX: add curve types here
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geom.type);
		}
	}

	/**
	 * Writes a "slim" Point (without endiannes, srid ant type, only the ordinates and measure. Used by writeGeometry as
	 * ell as writePointArray.
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writePoint(Point geom, ValueSetter dest)
	{
		dest.setDouble(geom.x);
		dest.setDouble(geom.y);

		if (geom.dimension == 3)
		{
			dest.setDouble(geom.z);
		}

		if (geom.haveMeasure)
		{
			dest.setDouble(geom.m);
		}
	}

	/**
	 * Write an Array of "slim" Points (without endianness, srid and type, part of LinearRing and Linestring, but not
	 * MultiPoint!
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writeOnlyPoints(Iterable<Point> geom, ValueSetter dest)
	{
		// number of points
		dest.setInt(count(geom));
		for (Point p : geom)
		{
			writePoint(p, dest);
		}
	}

	/**
	 * Gets the number of items.
	 * @param items items
	 * @return number of items
	 */
	private static <T> int count(Iterable<T> items)
	{
		// walk through all elements
		Iterator<T> it = items.iterator();
		int i = 0;
		while (it.hasNext())
		{
			it.next();
			i++;
		}
		return i;
	}

	/**
	 * Writes multiple geometries preceded by their count.
	 * @param geoms geometries
	 * @param dest writer
	 */
	private static <T extends Geometry> void writeMultiGeometry(Collection<T> geoms, ValueSetter dest)
	{
		dest.setInt(geoms.size());
		for (Geometry geom : geoms)
		{
			writeGeometry(geom, dest);
		}
	}

	private static void writeMultiPoint(MultiPoint geom, ValueSetter dest)
	{
		writeMultiGeometry(geom.getGeometries(), dest);
	}

	private static void writeLineString(LineString geom, ValueSetter dest)
	{
		writeOnlyPoints(geom, dest);
	}

	private static void writeLinearRing(LinearRing geom, ValueSetter dest)
	{
		writeOnlyPoints(geom, dest);
	}

	private static void writeRing(Geometry geom, ValueSetter dest)
	{
		// FIX
		// if(geom instanceof PointComposedGeom)
		// {
		// writeOnlyPoints((PointComposedGeom) geom, dest);
		// }else if(geom instanceof ComposedGeom)
		// {
		// //FIX
		// }else {
		throw new IllegalArgumentException("Unknown Geometry Type: " + geom.type);
		// }
	}

	private static void writePolygon(Polygon geom, ValueSetter dest)
	{
		dest.setInt(geom.numRings());
		for (int i = 0; i < geom.numRings(); i++)
		{
			writeLinearRing(geom.getRing(i), dest);
		}
	}

	private static void writeCurvePolygon(CurvePolygon geom, ValueSetter dest)
	{
		dest.setInt(geom.numRings());
		// FIX: wrong!
		for (int i = 0; i < geom.numRings(); i++)
		{
			writeRing(geom.getRing(i), dest);
		}
	}

	private static void writeMultiLineString(MultiLineString geom, ValueSetter dest)
	{
		writeMultiGeometry(geom.getGeometries(), dest);
	}

	private static void writeMultiPolygon(MultiPolygon geom, ValueSetter dest)
	{
		writeMultiGeometry(geom.getGeometries(), dest);
	}

	private static void writeCollection(GeometryCollection geom, ValueSetter dest)
	{
		writeMultiGeometry(geom.getGeometries(), dest);
	}

}
