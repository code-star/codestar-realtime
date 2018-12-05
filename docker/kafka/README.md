# Setup Kafka for local development
## TL;DR
```bash
./download-kafka-client.sh
./start.sh
open http://localhost:8000
```

# Requirements:
- [Docker](https://docs.docker.com/install/)
-  [jq](https://stedolan.github.io/jq/) (`brew install jq`)

## run `download-kafka-client.sh`
This will download the kafka client we will need to interact with Kafka from the command-line.
## run `./start.sh`
This will call `docker-compose` with the given `docker-compose.yml`. 
In this YAML the following services are defined:
1. Zookeeper ([confluentinc/cp-zookeeper:5.0.1](https://hub.docker.com/r/confluentinc/cp-zookeeper/))
2. 3x Kafka Brokers([confluentinc/cp-kafka:5.0.1](https://hub.docker.com/r/confluentinc/cp-kafka/))
3. Schema registry ([confluentinc/cp-schema-registry:5.0.1](https://hub.docker.com/r/confluentinc/cp-schema-registry/))
4. Kafka REST API ([confluentinc/cp-kafka-rest:5.0.1](https://hub.docker.com/r/confluentinc/cp-kafka-rest/))
5. Landoop's [Kafka Topics UI](https://github.com/Landoop/kafka-topics-ui) ([landoop/kafka-topics-ui:latest](https://hub.docker.com/r/landoop/kafka-topics-ui/))

The script will wait until Kafka is up, by checking if `_schema` is present in the JSON when calling the Kafka REST API on `localhost:8082/topics`. 
We use [jq](https://stedolan.github.io/jq/) to do command line json parsing.

Finally it will call the `createTopics.sh` script to create the `local.bus-positions` and `local.ns-trains-positions` topics.

You can then browse to [localhost:8000](http://localhost:8000) to view the Kafka Topics UI.

## run `./stop.sh`
Stops the running docker containers.