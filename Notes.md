
### Data sources

- Public transport, buses: https://openov.nl
- Public transport, buses + trains: https://www.ndovloket.nl
- Traffic: http://www.ndw.nu/pagina/nl/103/datalevering/120/open_data/
- Weather: http://weerlive.nl/delen.php
- Trains: https://developer.ns.nl
- Various: https://www.pubnub.com/developers/realtime-data-streams/
- Finance
- updates, events, ...
- Satellites: https://www.n2yo.com/
- NASA: https://open.nasa.gov/blog/the-open-api-universe-at-nasa/
- Flights: https://developer.flightstats.com/api-docs/flightstatus/v2
- Social media
    - Twitter
    - Foursquare: https://developer.foursquare.com/docs
    - Facebook
    - http://socialmention.com/
    
### OpenOV
- Realtime data for buses
    - http://gtfs.ovapi.nl/nl/vehiclePositions.pb
    - http://gtfs.ovapi.nl/nl/tripUpdates.pb
    - http://gtfs.ovapi.nl/nl/alerts.pb

### NDOV loket
- http://data.ndovloket.nl/REALTIME.TXT
- https://ndovloket.nl/documentatie.html
- Need ZeroMQ library to access data
- https://github.com/geertw/rdt-serviceinfo
    - "It is recommended to not connect directly to NDOVloket's ZeroMQ 
    server, but use some middleware instead called universal-sub-pubsub.", 
    which is written in C.

### GTFS (General Transit Feed Specification) Realtime protocol
- https://developers.google.com/transit/gtfs-realtime/
- Vehicle positions
   - Position: longitude and latitude, using WGS-84 coordinate system
   - Congestion Level
   - Occupancy status (experimental)
   - VehicleStopStatus (_Incoming at, Stopped at, In transit to_)
   - Vehicle Descriptor
- GTFS parser: https://github.com/azavea/gtfs-parser/tree/master/src/main/scala/com/azavea/gtfs
    
### Links
- https://ovzoeker.nl/locatie/52.08798:5.11997:14
- Map visualization: https://github.com/julienrf/closely
- OVZoeker and Closely use Leaflet
    - https://leafletjs.com/ an open-source JavaScript library
                             for mobile-friendly interactive maps
- http://opengeo.nl/ (owner of openOV and NDOVloket)