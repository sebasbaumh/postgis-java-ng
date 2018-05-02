# PostGIS Java bindings (Next Generation)
This project contains Java bindings for using PostGIS geometries coming from a PostgreSQL database.

This project is based on [postgis-java](https://github.com/postgis/postgis-java) and I want to thank its authors for their work.

Goals to improve the existing [postgis-java](https://github.com/postgis/postgis-java):
* Use generic Java types where possible
* Clean up code to basically only work on [WKB](https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary)/EWKB implementations to reduce code duplication and focus on the actual database format
* Support for the latest PostgreSQL (9.6+) and PostGIS versions (2.1+)
* Support for JDK 8+

*Please note: I will add further licensing information reflecting the base of this project once it is set up and some clean up has taken place.*
