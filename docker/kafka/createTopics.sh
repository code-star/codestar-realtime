#!/usr/bin/env bash

thisDir=$(dirname $0)
prefix="local"

# we use 9 partitions. This offers ample parallelism. And if our messages get very big the buffers on the cluster are acceptable.
# for comparison: We chose 9 since that is a multiple of the nr of brokers which distributes out the work across the
# brokers
kafkaHome=$thisDir/kafka_2.12-2.1.0
$kafkaHome/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 9 --replication-factor 1 --topic $prefix.bus-positions
$kafkaHome/bin/kafka-topics.sh --zookeeper localhost:2181 --create --partitions 9 --replication-factor 1 --topic $prefix.ns-trains-positions

