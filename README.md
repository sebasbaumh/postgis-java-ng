# PostGIS Java bindings (Next Generation)
This project contains Java bindings for using [PostGIS](https://postgis.net/) geometries coming from a [PostgreSQL](https://www.postgresql.org/) database.

This project is based on [postgis-java](https://github.com/postgis/postgis-java) and I want to thank its authors for their work.

Goals to improve the existing [postgis-java](https://github.com/postgis/postgis-java):
* Use generic Java types where possible
* Clean up code to basically only work on [WKB](https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary)/EWKB implementations to reduce code duplication and focus on the actual database format
* Support for the latest PostgreSQL (9.6+) and PostGIS versions (2.1+)
* Support for JDK 8+

*Please note: I will add further licensing information reflecting the base of this project once it is set up and some clean up has taken place.*

## How to run tests utilizing a PostgreSQL server

You will need a PostgreSQL server with installed PostGIS extension for some of the tests.

In this example the server is named `MyServer` and the database `UnitTestDB`. The database can be empty except installing the PostGIS extension.

You should set up a database user for the unit tests, which has access rights to this database and only to this one.
In this example the user is called `unittest` and has the password `CHANGEME`.

To run the unit tests accessing the server, add the following to your VM arguments (eclipse Run Configuration->Arguments->VM arguments):

`-DtestJdbcUrl="jdbc:postgresql://MyServer/UnitTestDB" -DtestJdbcUsername="unittest" -DtestJdbcPassword="CHANGEME"`
