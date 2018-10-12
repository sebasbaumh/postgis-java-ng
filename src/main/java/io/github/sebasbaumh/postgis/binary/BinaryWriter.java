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

import io.github.sebasbaumh.postgis.CircularString;
import io.github.sebasbaumh.postgis.CurvePolygon;
import io.github.sebasbaumh.postgis.Geometry;
import io.github.sebasbaumh.postgis.GeometryCollection;
import io.github.sebasbaumh.postgis.LineString;
import io.github.sebasbaumh.postgis.MultiLineString;
import io.github.sebasbaumh.postgis.MultiPoint;
import io.github.sebasbaumh.postgis.MultiPolygon;
import io.github.sebasbaumh.postgis.Point;
import io.github.sebasbaumh.postgis.Polygon;
import io.github.sebasbaumh.postgis.PostGisUtil;

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

	private static void writeCurvePolygon(CurvePolygon geom, ValueSetter dest)
	{
		dest.setInt(geom.numRings());
		// FIX: wrong!
		for (int i = 0; i < geom.numRings(); i++)
		{
			writeRing(geom.getRing(i), dest);
		}
	}

	/**
	 * Parse a geometry starting at offset.
	 * @param geom the geometry to write
	 * @param dest the value setting to be used for writing
	 */
	protected static void writeGeometry(Geometry geom, ValueSetter dest)
	{
		// write endian flag, NDR (little endian)
		dest.setByte(dest.getEndian());

		// write typeword
		int typeword = geom.getType();
		if (geom.is3d())
		{
			typeword |= 0x80000000;
		}
		if (geom.hasMeasure())
		{
			typeword |= 0x40000000;
		}
		if (geom.getSrid() != Geometry.UNKNOWN_SRID)
		{
			typeword |= 0x20000000;
		}
		dest.setInt(typeword);

		if (geom.getSrid() != Geometry.UNKNOWN_SRID)
		{
			dest.setInt(geom.getSrid());
		}

		switch (geom.getType())
		{
			case Point.TYPE:
				writePoint((Point) geom, dest);
				break;
			case LineString.TYPE:
				writePoints((LineString) geom, dest);
				break;
			case Polygon.TYPE:
				writePolygon((Polygon) geom, dest);
				break;
			case MultiPoint.TYPE:
				writeMultiGeometry(((MultiPoint) geom).getGeometries(), dest);
				break;
			case MultiLineString.TYPE:
				writeMultiGeometry(((MultiLineString) geom).getGeometries(), dest);
				break;
			case MultiPolygon.TYPE:
				writeMultiGeometry(((MultiPolygon) geom).getGeometries(), dest);
				break;
			case GeometryCollection.TYPE:
				writeMultiGeometry(((GeometryCollection) geom).getGeometries(), dest);
				break;
			case CircularString.TYPE:
				writePoints((CircularString) geom, dest);
				break;
			case CurvePolygon.TYPE:
				writeCurvePolygon((CurvePolygon) geom, dest);
				break;
			// FIX: add curve types here
			default:
				throw new IllegalArgumentException("Unknown Geometry Type: " + geom.getType());
		}
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
		ByteSetter bytes = new ByteSetter();
		writeGeometry(geom, new ValueSetter(bytes));
		return bytes.toString();
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

	/**
	 * Writes a "slim" Point (without endiannes, srid ant type, only the ordinates and measure. Used by writeGeometry as
	 * ell as writePointArray.
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writePoint(Point geom, ValueSetter dest)
	{
		dest.setDouble(geom.getX());
		dest.setDouble(geom.getY());
		// write z coordinate?
		if (geom.is3d())
		{
			dest.setDouble(geom.getZ());
		}
		// write measure?
		if (geom.hasMeasure())
		{
			dest.setDouble(geom.getM());
		}
	}

	/**
	 * Write an Array of "slim" Points (without endianness, srid and type, part of LinearRing and Linestring, but not
	 * MultiPoint!
	 * @param geom geometry
	 * @param dest writer
	 */
	private static void writePoints(Iterable<Point> geom, ValueSetter dest)
	{
		// number of points
		dest.setInt(PostGisUtil.size(geom));
		for (Point p : geom)
		{
			writePoint(p, dest);
		}
	}

	private static void writePolygon(Polygon geom, ValueSetter dest)
	{
		dest.setInt(geom.numRings());
		for (int i = 0; i < geom.numRings(); i++)
		{
			writePoints(geom.getRing(i), dest);
		}
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
		throw new IllegalArgumentException("Unknown Geometry Type: " + geom.getType());
		// }
	}

}
