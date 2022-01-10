# PostGIS Java bindings (Next Generation) #

[![CodeQL](https://github.com/sebasbaumh/postgis-java-ng/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/sebasbaumh/postgis-java-ng/actions/workflows/codeql-analysis.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sebasbaumh/postgis-java-ng)](https://search.maven.org/artifact/io.github.sebasbaumh/postgis-java-ng)
[![javadoc](https://javadoc.io/badge2/io.github.sebasbaumh/postgis-java-ng/javadoc.svg)](https://javadoc.io/doc/io.github.sebasbaumh/postgis-java-ng/latest/index.html)
[![License](https://img.shields.io/github/license/sebasbaumh/postgis-java-ng.svg)](https://github.com/sebasbaumh/postgis-java-ng/blob/master/LICENSE)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=sebasbaumh_postgis-java-ng&metric=ncloc)](https://sonarcloud.io/dashboard?id=sebasbaumh_postgis-java-ng)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=sebasbaumh_postgis-java-ng&metric=security_rating)](https://sonarcloud.io/dashboard?id=sebasbaumh_postgis-java-ng)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sebasbaumh_postgis-java-ng&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=sebasbaumh_postgis-java-ng)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=sebasbaumh_postgis-java-ng&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=sebasbaumh_postgis-java-ng)

This project contains Java bindings for using [PostGIS](https://postgis.net/) geometries coming from a [PostgreSQL](https://www.postgresql.org/) database.  
It is originally based on [postgis-java](https://github.com/postgis/postgis-java) and I want to thank its authors here for their work.

**Project goals and improvements:**
* Support for geometries containing arcs like `CIRCULARSTRING` or `CURVEPOLYGON`
* Support for PostGIS [geography datatype](https://postgis.net/docs/using_postgis_dbmanagement.html#PostGIS_Geography)
* Use generic Java types where possible and simplify/streamline API
* Clean up code to basically only work on [WKB](https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary)/EWKB implementations to reduce code duplication and focus on the actual database format
* Support for the latest PostgreSQL and PostGIS versions
    * Recommended are PostgreSQL 14 and PostGIS 3.2.0
    * Supported are versions starting from PostgreSQL 9.6 and PostGIS 2.3
* Support for JDK 11+ (there is an older [branch for JDK 8](https://github.com/sebasbaumh/postgis-java-ng/tree/jdk8))
* The license is still LGPL

**Supported [geometry types](https://postgis.net/docs/using_postgis_dbmanagement.html#RefObject):**
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

```xml
<dependency>
	<groupId>io.github.sebasbaumh</groupId>
	<artifactId>postgis-java-ng</artifactId>
	<version>22.0.0</version>
</dependency>
```

The version reflects the year of the release, e.g. 22.0.0 is a version released in 2022.

The API differs a bit from [postgis-java](https://github.com/postgis/postgis-java) with the main point being a different namespace (`io.github.sebasbaumh.postgis`) as publishing a project to Maven Central requires to own that namespace.
In addition the class structure is a bit different (see below) to support arc geometries and reduce boilerplate code, but you should be able to adapt to it easily.
The implementations of the parser and writer for the geometries have been heavily reworked to speed up processing and reduce complexity.
	
## Hierarchy of geometry classes: ##

![Hierarchy of geometry classes](ClassHierarchy.png)

## How to run tests utilizing a PostgreSQL server ##

You will need a PostgreSQL server with installed PostGIS extension for some of the tests.

In this example the server is named `MyServer` and the database `UnitTestDB`. The database can be empty except installing the PostGIS extension.

You should set up a database user for the unit tests, which has access rights to this database and only to this one.
In this example the user is called `unittest` and has the password `CHANGEME`.

To run the unit tests accessing the server, add the following to your VM arguments (eclipse Run Configuration->Arguments->VM arguments):

`-DtestJdbcUrl="jdbc:postgresql://MyServer/UnitTestDB" -DtestJdbcUsername="unittest" -DtestJdbcPassword="CHANGEME"`

Or add the following Maven build parameters to the launch configuration in eclipse:

|Parameter Name|Value|
|--------------|-----|
|`testJdbcUrl`|`jdbc:postgresql://MyServer/UnitTestDB`|
|`testJdbcUsername`|`unittest`|
|`testJdbcPassword`|`CHANGEME`|

*There are also local tests contained in the project, so you are still able to test most parts without specifying a PostgreSQL server. And the test console output will show if tests were run with or without a database.*
