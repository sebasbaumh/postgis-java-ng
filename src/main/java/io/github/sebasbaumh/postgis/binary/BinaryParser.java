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
 * Parse binary representation of geometries.
 * 
 * It should be easy to add char[] and CharSequence ByteGetter instances,
 * although the latter one is not compatible with older jdks.
 * 
 * I did not implement real unsigned 32-bit integers or emulate them with long,
 * as both java Arrays and Strings currently can have only 2^31-1 elements
 * (bytes), so we cannot even get or build Geometries with more than approx.
 * 2^28 coordinates (8 bytes each).
 * 
 * @author {@literal Markus Schaber <markus.schaber@logix-tt.com>}
 * 
 */
public class BinaryParser {

    /**
     * Get the appropriate ValueGetter for my endianness
     * 
     * @param bytes
     *            The appropriate Byte Getter
     * 
     * @return the ValueGetter
     */
    public static ValueGetter valueGetterForEndian(ByteGetter bytes) {
	if (bytes.get(0) == ValueGetter.XDR.NUMBER) { // XDR
	    return new ValueGetter.XDR(bytes);
	} else if (bytes.get(0) == ValueGetter.NDR.NUMBER) {
	    return new ValueGetter.NDR(bytes);
	} else {
	    throw new IllegalArgumentException("Unknown Endian type:" + bytes.get(0));
	}
    }

    /**
     * Parse a hex encoded geometry
     * 
     * @param value
     *            String containing the data to be parsed
     * @return resulting geometry for the parsed data
     */
    public static Geometry parse(String value) {
	ByteGetter bytes = new ByteGetter(value);
	return parseGeometry(valueGetterForEndian(bytes));
    }

    /**
     * Parse a geometry starting at offset.
     *
     * @param data
     *            ValueGetter with the data to be parsed
     * @return the parsed geometry
     */
    protected static Geometry parseGeometry(ValueGetter data) {
	byte endian = data.getByte(); // skip and test endian flag
	if (endian != data.endian) {
	    throw new IllegalArgumentException("Endian inconsistency!");
	}
	int typeword = data.getInt();

	int realtype = typeword & 0x1FFFFFFF; // cut off high flag bits

	boolean haveZ = (typeword & 0x80000000) != 0;
	boolean haveM = (typeword & 0x40000000) != 0;
	boolean haveS = (typeword & 0x20000000) != 0;

	int srid = Geometry.UNKNOWN_SRID;

	if (haveS) {
	    srid = Geometry.parseSRID(data.getInt());
	}
	Geometry result1;
	switch (realtype) {
	case Geometry.POINT:
	    result1 = parsePoint(data, haveZ, haveM);
	    break;
	case Geometry.LINESTRING:
	    result1 = parseLineString(data, haveZ, haveM);
	    break;
	case Geometry.POLYGON:
	    result1 = parsePolygon(data, haveZ, haveM);
	    break;
	case Geometry.MULTIPOINT:
	    result1 = parseMultiPoint(data);
	    break;
	case Geometry.MULTILINESTRING:
	    result1 = parseMultiLineString(data);
	    break;
	case Geometry.MULTIPOLYGON:
	    result1 = parseMultiPolygon(data);
	    break;
	case Geometry.GEOMETRYCOLLECTION:
	    result1 = parseCollection(data);
	    break;
	case Geometry.CIRCULARSTRING:
	    result1 = parseCircularString(data, haveZ, haveM);
	    break;
	case Geometry.CURVEPOLYGON:
	    result1 = parseCurvePolygon(data, haveZ, haveM);
	    break;
	// FIX: add curve types here
	default:
	    throw new IllegalArgumentException("Unknown Geometry Type: " + realtype);
	}

	Geometry result = result1;

	if (srid != Geometry.UNKNOWN_SRID) {
	    result.setSrid(srid);
	}
	return result;
    }

    private static Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM) {
	double X = data.getDouble();
	double Y = data.getDouble();
	Point result;
	if (haveZ) {
	    double Z = data.getDouble();
	    result = new Point(X, Y, Z);
	} else {
	    result = new Point(X, Y);
	}

	if (haveM) {
	    result.setM(data.getDouble());
	}

	return result;
    }

    /** Parse an Array of "full" Geometries */
    private static void parseGeometryArray(ValueGetter data, Geometry[] container) {
	for (int i = 0; i < container.length; i++) {
	    container[i] = parseGeometry(data);
	}
    }

    /** Parse an Array of "full" Geometries */
    @SuppressWarnings("unchecked")
	private static <T extends Geometry> ArrayList<T> parseGeometries(Class<T> clazz, ValueGetter data, int count) {
    	ArrayList<T> l=new ArrayList<T>(count);
	for (int i = 0; i < count; i++) {
		Geometry geom=parseGeometry(data);
		if(clazz.isInstance(geom))
		{
	    l.add((T) geom);
		}else {
			throw new IllegalArgumentException("expected: "+clazz.getCanonicalName()+" got: "+geom.getClass().getCanonicalName());
		}
	}
	return l;
    }

    /**
     * Parse an Array of "slim" Points (without endianness and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     * 
     * @param haveZ
     * @param haveM
     */
    private static Point[] parsePointArray(ValueGetter data, boolean haveZ, boolean haveM) {
	int count = data.getInt();
	Point[] result = new Point[count];
	for (int i = 0; i < count; i++) {
	    result[i] = parsePoint(data, haveZ, haveM);
	}
	return result;
    }

    /**
     * Parse an Array of "slim" Points (without endianness and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     * 
     * @param haveZ
     * @param haveM
     */
    private static ArrayList<Point> parsePoints(ValueGetter data, boolean haveZ, boolean haveM) {
	int count = data.getInt();
	ArrayList<Point> l=new ArrayList<Point>(count);
	for (int i = 0; i < count; i++) {
	    l.add(parsePoint(data, haveZ, haveM));
	}
	return l;
    }

    private static MultiPoint parseMultiPoint(ValueGetter data) {
    	int count=data.getInt();
    	return new MultiPoint(parseGeometries(Point.class, data, count));
    }

    private static LineString parseLineString(ValueGetter data, boolean haveZ, boolean haveM) {
	return new LineString(parsePoints(data, haveZ, haveM));
    }

    private static CircularString parseCircularString(ValueGetter data, boolean haveZ, boolean haveM) {
	return new CircularString(parsePoints(data, haveZ, haveM));
    }

    private static LinearRing parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM) {
	return new LinearRing(parsePoints(data, haveZ, haveM));
    }

    private static Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM) {
	int count = data.getInt();
	ArrayList<LinearRing> rings = new ArrayList<LinearRing>(count);
	for (int i = 0; i < count; i++) {
	    rings.add(parseLinearRing(data, haveZ, haveM));
	}
	return new Polygon(rings);
    }

    private static CurvePolygon parseCurvePolygon(ValueGetter data, boolean haveZ, boolean haveM) {
	int count = data.getInt();
	Geometry[] geoms = new Geometry[count];
	//FIX: wrong
	parseGeometryArray(data, geoms);
//	return new CurvePolygon(geoms);
	//FIX
	throw new UnsupportedOperationException();
    }

    private static MultiLineString parseMultiLineString(ValueGetter data) {
	int count=data.getInt();
	return new MultiLineString(parseGeometries(LineString.class, data, count));
    }

    private static MultiPolygon parseMultiPolygon(ValueGetter data) {
	int count=data.getInt();
	return new MultiPolygon(parseGeometries(Polygon.class, data, count));
    }

    private static GeometryCollection parseCollection(ValueGetter data) {
	int count=data.getInt();
	return new GeometryCollection(parseGeometries(Geometry.class, data, count));
    }
}
