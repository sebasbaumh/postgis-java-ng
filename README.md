# PostGIS Java bindings (Next Generation)
This project contains Java bindings for using [PostGIS](https://postgis.net/) geometries coming from a [PostgreSQL](https://www.postgresql.org/) database.

*This project is based on [postgis-java](https://github.com/postgis/postgis-java) and first of all I want to thank its authors for their work.*

**Project goals and improvements to the existing [postgis-java](https://github.com/postgis/postgis-java):**
* Support for geometries containing arcs like `CIRCULARSTRING` or `CURVEPOLYGON`
* Use generic Java types where possible and simplify API
* Clean up code to basically only work on [WKB](https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary)/EWKB implementations to reduce code duplication and focus on the actual database format
* Support for the latest PostgreSQL (9.6+) and PostGIS versions (2.1+)
* Support for JDK 8+
* The license is still LGPL

**Supported [geometry types](http://postgis.net/docs/manual-2.5/using_postgis_dbmanagement.html#RefObject):**
* Point
* LineString
* CircularString
* CompoundCurve
* Polygon
* CurvePolygon
* MultiPoint
* MultiLineString
* MultiCurve
* MultiPolygon
* MultiSurface
* GeometryCollection

## How to use it ##
There is a Maven artifact in the official Maven repository, so just add this to your Maven POM:

	<dependency>
		<groupId>io.github.sebasbaumh</groupId>
		<artifactId>postgis-java-ng</artifactId>
		<version>1.0.6</version>
	</dependency>

The API differs a bit from [postgis-java](https://github.com/postgis/postgis-java), the main point is a different namespace (`io.github.sebasbaumh.postgis`) as I might not be able to publish an artificat under the original namespace.
In addition the class structure might be a bit different to support arc geometries and reduce boilerplate code, but you should be able to adapt to it easily.
	
## How to run tests utilizing a PostgreSQL server

You will need a PostgreSQL server with installed PostGIS extension for some of the tests.

In this example the server is named `MyServer` and the database `UnitTestDB`. The database can be empty except installing the PostGIS extension.

You should set up a database user for the unit tests, which has access rights to this database and only to this one.
In this example the user is called `unittest` and has the password `CHANGEME`.

To run the unit tests accessing the server, add the following to your VM arguments (eclipse Run Configuration->Arguments->VM arguments):

`-DtestJdbcUrl="jdbc:postgresql://MyServer/UnitTestDB" -DtestJdbcUsername="unittest" -DtestJdbcPassword="CHANGEME"`
