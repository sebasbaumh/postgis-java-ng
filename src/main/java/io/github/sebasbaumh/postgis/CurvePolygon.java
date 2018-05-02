/*
 * Polygon.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - geometry model
 * 
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
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

package io.github.sebasbaumh.postgis;

import java.sql.SQLException;

/**
 * A CURVEPOLYGON is just like a polygon, with an outer ring and zero or more
 * inner rings. The difference is that a ring can take the form of a circular
 * string, linear string or compound string.
 * 
 * @author Sebastian Baumhekel
 */
public class CurvePolygon extends ComposedGeom {
    /* JDK 1.5 Serialization */
    private static final long serialVersionUID = 0x100;

    public CurvePolygon() {
	super(CURVEPOLYGON);
    }

    public CurvePolygon(Geometry[] rings) {
	super(CURVEPOLYGON, rings);
    }

    public CurvePolygon(String value) throws SQLException {
	this(value, false);
    }

    public CurvePolygon(String value, boolean haveM) throws SQLException {
	super(CURVEPOLYGON, value, haveM);
    }

    @Override
    protected Geometry createSubGeomInstance(String token, boolean haveM) throws SQLException {
	// detect simple linear rings
	if (token.startsWith("(") && token.endsWith(")")) {
	    return new LineString(token, haveM);
	}
	// circular strings and compound curves
	return PGgeometry.geomFromString(token, haveM);
    }

    @Override
    protected Geometry[] createSubGeomArray(int ringcount) {
	return new Geometry[ringcount];
    }

    public int numRings() {
	return subgeoms.length;
    }

    public Geometry getRing(int idx) {
	if (idx >= 0 & idx < subgeoms.length) {
	    return (Geometry) subgeoms[idx];
	} else {
	    return null;
	}
    }
}
