# codestar-realtime
Real-time demo (work in progress).

### How to run

Download Kafka client and start the server:

    ./docker/kafka/download-kafka-client.sh
    ./docker/kafka/start.sh
    
Data producer(s):
    
`nl.codestar.producers.TrainLocationProducer` (for train positions from NDOVloket)   
`nl.codestar.producers.OpenOVProducer` (for bus positions from OpenOV)

Consumer service:

`nl.codestar.consumers.positions.PositionsServer` (only for trains for the moment)

Try it at <http://localhost:8080/ns/positions?n=52.10057991947965&e=5.166184343397618&s=52.07639948922387&w=5.094086565077306>