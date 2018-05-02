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

import io.github.sebasbaumh.postgis.CircularString;
import io.github.sebasbaumh.postgis.ComposedGeom;
import io.github.sebasbaumh.postgis.CurvePolygon;
import io.github.sebasbaumh.postgis.Geometry;
import io.github.sebasbaumh.postgis.GeometryCollection;
import io.github.sebasbaumh.postgis.LineString;
import io.github.sebasbaumh.postgis.LinearRing;
import io.github.sebasbaumh.postgis.MultiCurve;
import io.github.sebasbaumh.postgis.MultiLineString;
import io.github.sebasbaumh.postgis.MultiPoint;
import io.github.sebasbaumh.postgis.MultiPolygon;
import io.github.sebasbaumh.postgis.Point;
import io.github.sebasbaumh.postgis.PointComposedGeom;
import io.github.sebasbaumh.postgis.Polygon;

/**
 * Create binary representation of geometries. Currently, only text rep (hexed)
 * implementation is tested.
 * 
 * It should be easy to add char[] and CharSequence ByteGetter instances,
 * although the latter one is not compatible with older jdks.
 * 
 * I did not implement real unsigned 32-bit integers or emulate them with long,
 * as both java Arrays and Strings currently can have only 2^31-1 elements
 * (bytes), so we cannot even get or build Geometries with more than approx.
 * 2^28 coordinates (8 bytes each).
 * 
 * @author markus.schaber@logi-track.com
 * 
 */
public class BinaryWriter {

    /**
     * Get the appropriate ValueGetter for my endianness
     * 
     * @param bytes
     *            The ByteSetter to use
     * @param endian
     *            the endian for the ValueSetter to use
     * @return the ValueGetter
     */
    public static ValueSetter valueSetterForEndian(ByteSetter bytes, byte endian) {
	if (endian == ValueSetter.XDR.NUMBER) { // XDR
	    return new ValueSetter.XDR(bytes);
	} else if (endian == ValueSetter.NDR.NUMBER) {
	    return new ValueSetter.NDR(bytes);
	} else {
	    throw new IllegalArgumentException("Unknown Endian type:" + endian);
	}
    }

    /**
     * Write a hex encoded geometry
     * 
     * Is synchronized to protect offset counter. (Unfortunately, Java does not have
     * neither call by reference nor multiple return values.) This is a TODO item.
     * 
     * The geometry you put in must be consistent, geom.checkConsistency() must
     * return true. If not, the result may be invalid WKB.
     * 
     * @see Geometry#checkConsistency() the consistency checker
     *
     * @param geom
     *            the geometry to be written
     * @param REP
     *            endianness to write the bytes with
     * @return String containing the hex encoded geometry
     */
    public synchronized String writeHexed(Geometry geom, byte REP) {
	int length = estimateBytes(geom);
	ByteSetter.StringByteSetter bytes = new ByteSetter.StringByteSetter(length);
	writeGeometry(geom, valueSetterForEndian(bytes, REP));
	return bytes.result();
    }

    public synchronized String writeHexed(Geometry geom) {
	return writeHexed(geom, ValueSetter.NDR.NUMBER);
    }

    /**
     * Write a binary encoded geometry.
     * 
     * Is synchronized to protect offset counter. (Unfortunately, Java does not have
     * neither call by reference nor multiple return values.) This is a TODO item.
     * 
     * The geometry you put in must be consistent, geom.checkConsistency() must
     * return true. If not, the result may be invalid WKB.
     * 
     * @see Geometry#checkConsistency()
     *
     * @param geom
     *            the geometry to be written
     * @param REP
     *            endianness to write the bytes with
     * @return byte array containing the encoded geometry
     */
    public synchronized byte[] writeBinary(Geometry geom, byte REP) {
	int length = estimateBytes(geom);
	ByteSetter.BinaryByteSetter bytes = new ByteSetter.BinaryByteSetter(length);
	writeGeometry(geom, valueSetterForEndian(bytes, REP));
	return bytes.result();
    }

    public synchronized byte[] writeBinary(Geometry geom) {
	return writeBinary(geom, ValueSetter.NDR.NUMBER);
    }

    /**
     * Parse a geometry starting at offset.
     * 
     * @param geom
     *            the geometry to write
     * @param dest
     *            the value setting to be used for writing
     */
    protected static void writeGeometry(Geometry geom, ValueSetter dest) {
	// write endian flag
	dest.setByte(dest.endian);

	// write typeword
	int typeword = geom.type;
	if (geom.dimension == 3) {
	    typeword |= 0x80000000;
	}
	if (geom.haveMeasure) {
	    typeword |= 0x40000000;
	}
	if (geom.srid != Geometry.UNKNOWN_SRID) {
	    typeword |= 0x20000000;
	}

	dest.setInt(typeword);

	if (geom.srid != Geometry.UNKNOWN_SRID) {
	    dest.setInt(geom.srid);
	}

	switch (geom.type) {
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
     * Writes a "slim" Point (without endiannes, srid ant type, only the ordinates
     * and measure. Used by writeGeometry as ell as writePointArray.
     * 
     * @param geom
     *            geometry
     * @param dest
     *            writer
     */
    private static void writePoint(Point geom, ValueSetter dest) {
	dest.setDouble(geom.x);
	dest.setDouble(geom.y);

	if (geom.dimension == 3) {
	    dest.setDouble(geom.z);
	}

	if (geom.haveMeasure) {
	    dest.setDouble(geom.m);
	}
    }

    /**
     * Write an Array of "slim" Points (without endianness, srid and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     * 
     * @param geom
     *            geometry
     * @param dest
     *            writer
     */
    private static void writeOnlyPoints(PointComposedGeom geom, ValueSetter dest) {
	Point[] points = geom.getPoints();
	// number of points
	dest.setInt(points.length);
	for (Point p : points) {
	    writePoint(p, dest);
	}
    }

    /**
     * Writes multiple geometries preceded by their count.
     * 
     * @param geoms
     *            geometries
     * @param dest
     *            writer
     */
    private static void writeMultiGeometry(Geometry[] geoms, ValueSetter dest) {
	dest.setInt(geoms.length);
	for (Geometry geom : geoms) {
	    writeGeometry(geom, dest);
	}
    }

    private static void writeMultiPoint(MultiPoint geom, ValueSetter dest) {
	writeMultiGeometry(geom.getPoints(), dest);
    }

    private static void writeLineString(LineString geom, ValueSetter dest) {
	writeOnlyPoints(geom, dest);
    }

    private static void writeLinearRing(LinearRing geom, ValueSetter dest) {
	writeOnlyPoints(geom, dest);
    }
    
    private static void writeRing(Geometry geom,ValueSetter dest)
    {
	if(geom instanceof PointComposedGeom)
	{
	    writeOnlyPoints((PointComposedGeom) geom, dest);
	}else if(geom instanceof ComposedGeom)
	{
	    //FIX
	}else {
	    throw new IllegalArgumentException("Unknown Geometry Type: " + geom.type);
	}
    }

    private static void writePolygon(Polygon geom, ValueSetter dest) {
	dest.setInt(geom.numRings());
	for (int i = 0; i < geom.numRings(); i++) {
	    writeLinearRing(geom.getRing(i), dest);
	}
    }

    private static void writeCurvePolygon(CurvePolygon geom, ValueSetter dest) {
	dest.setInt(geom.numRings());
	//FIX: wrong!
	for (int i = 0; i < geom.numRings(); i++) {
	    writeRing(geom.getRing(i), dest);
	}
    }

    private static void writeMultiLineString(MultiLineString geom, ValueSetter dest) {
	writeMultiGeometry(geom.getLines(), dest);
    }

    private static void writeMultiPolygon(MultiPolygon geom, ValueSetter dest) {
	writeMultiGeometry(geom.getPolygons(), dest);
    }

    private static void writeCollection(GeometryCollection geom, ValueSetter dest) {
	writeMultiGeometry(geom.getGeometries(), dest);
    }

    /**
     * Estimate how much bytes a geometry will need in WKB.
     *
     * @param geom
     *            Geometry to estimate.
     * @return estimated number of bytes
     */
    protected static int estimateBytes(Geometry geom) {
	int result = 0;

	// write endian flag
	result += 1;

	// write typeword
	result += 4;

	if (geom.srid != Geometry.UNKNOWN_SRID) {
	    result += 4;
	}

	switch (geom.type) {
	case Geometry.POINT:
	    result += estimatePoint((Point) geom);
	    break;
	case Geometry.LINESTRING:
	    result += estimateLineString((LineString) geom);
	    break;
	case Geometry.POLYGON:
	    result += estimatePolygon((Polygon) geom);
	    break;
	case Geometry.MULTIPOINT:
	    result += estimateMultiPoint((MultiPoint) geom);
	    break;
	case Geometry.MULTILINESTRING:
	    result += estimateMultiLineString((MultiLineString) geom);
	    break;
	case Geometry.MULTIPOLYGON:
	    result += estimateMultiPolygon((MultiPolygon) geom);
	    break;
	case Geometry.GEOMETRYCOLLECTION:
	    result += estimateCollection((GeometryCollection) geom);
	    break;
	case Geometry.CIRCULARSTRING:
	    result += estimatePointComposedGeom((CircularString) geom);
	    break;
	case Geometry.CURVEPOLYGON:
	    result += estimateCurvePolygon((CurvePolygon) geom);
	    break;
	// FIX: add curve types here
	default:
	    throw new IllegalArgumentException("Unknown Geometry Type: " + geom.type);
	}
	return result;
    }

    private static int estimatePoint(Point geom) {
	// x, y both have 8 bytes
	int result = 16;
	if (geom.dimension == 3) {
	    result += 8;
	}

	if (geom.haveMeasure) {
	    result += 8;
	}
	return result;
    }

    /**
     * Write an Array of "slim" Points (without endianness and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     */
    private static int estimatePointComposedGeom(PointComposedGeom geom) {
	Point[] points = geom.getPoints();
	// number of points
	int result = 4;

	// And the amount of the points itsself, in consistent geometries
	// all points have equal size.
	if (points.length > 0) {
	    result += points.length * estimatePoint(points[0]);
	}
	return result;
    }

    private static int estimateLineString(LineString geom) {
	return estimatePointComposedGeom(geom);
    }

    private static int estimateLinearRing(LinearRing geom) {
	return estimatePointComposedGeom(geom);
    }

    private static int estimatePolygon(Polygon geom) {
	// int length
	int result = 4;
	for (int i = 0; i < geom.numRings(); i++) {
	    result += estimateLinearRing(geom.getRing(i));
	}
	return result;
    }

    private static int estimateCurvePolygon(CurvePolygon geom) {
	// int length
	int result = 4;
	for (int i = 0; i < geom.numRings(); i++) {
	    Geometry ring=(Geometry) geom.getRing(i);
	    if(ring instanceof PointComposedGeom) {
	    result += estimatePointComposedGeom((PointComposedGeom)ring);
	    }else if(ring instanceof MultiCurve) {
		    result += estimateMultiGeometry(((MultiCurve)ring).getLines());
	    }
	}
	return result;
    }

    private static int estimateMultiGeometry(Geometry[] geoms) {
	// 4-byte count + subgeometries
	int result = 4;
	for (Geometry geom : geoms) {
	    result += estimateBytes(geom);
	}
	return result;
    }

    private static int estimateMultiPoint(MultiPoint geom) {
	return estimateMultiGeometry(geom.getPoints());
    }

    private static int estimateMultiLineString(MultiLineString geom) {
	return estimateMultiGeometry(geom.getLines());
    }

    private static int estimateMultiPolygon(MultiPolygon geom) {
	return estimateMultiGeometry(geom.getPolygons());
    }

    private static int estimateCollection(GeometryCollection geom) {
	return estimateMultiGeometry(geom.getGeometries());
    }
}
