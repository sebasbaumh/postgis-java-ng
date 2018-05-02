/*
 * MultiCurve.java
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
 * The MULTICURVE is a collection of curves, which can include linear strings, circular strings or compound strings.
 * @author Sebastian Baumhekel
 */
public class MultiCurve extends ComposedGeom {
    /* JDK 1.5 Serialization */
    private static final long serialVersionUID = 0x100;

    double len = -1;

    public int hashCode() {
        return super.hashCode() ^ (int) this.length();
    }

    public MultiCurve() {
        super(MULTICURVE);
    }

    public MultiCurve(LineString[] lines) {
        super(MULTICURVE, lines);
    }

    public MultiCurve(String value) throws SQLException {
        this(value, false);
    }

    public MultiCurve(String value, boolean haveM) throws SQLException {
        super(MULTICURVE, value, haveM);
    }

    protected Geometry createSubGeomInstance(String token, boolean haveM) throws SQLException {
	// detect simple linear rings
	if (token.startsWith("(") && token.endsWith(")")) {
	    return new LineString(token, haveM);
	}
	// circular strings and compound curves
	return PGgeometry.geomFromString(token, haveM);
    }

    protected Geometry[] createSubGeomArray(int nlines) {
        return new Geometry[nlines];
    }

    public int numLines() {
        return subgeoms.length;
    }

    public Geometry[] getLines() {
        return (Geometry[]) subgeoms.clone();
    }

    public Geometry getLine(int idx) {
        if (idx >= 0 & idx < subgeoms.length) {
            return (Geometry) subgeoms[idx];
        } else {
            return null;
        }
    }

    public double length() {
        if (len < 0) {
            double sum = 0;
            for(Geometry geom: subgeoms)
            {
                sum+=calcLength(geom);
            }
            len = sum;
        }
        return len;
    }
    
    private static double calcLength(Geometry geom)
    {
	if(geom instanceof LineBasedGeom)
	{
	    return ((LineBasedGeom) geom).length();
	}
	if(geom instanceof ComposedGeom)
	{
	    double l=0;
	    for(Geometry subGeom:((ComposedGeom) geom).subgeoms)
            {
                l+=calcLength(subGeom);
            }
	    return l;
	}
	
	return 0;
    }
}
