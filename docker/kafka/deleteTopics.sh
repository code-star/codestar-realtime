#!/usr/bin/env bash

thisDir=$(dirname $0)
prefix="local"

kafkaHome=$thisDir/kafka_2.12-2.1.0
$kafkaHome/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic $prefix.bus-positions
$kafkaHome/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic $prefix.ns-trains-positions
$kafkaHome/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic $prefix.all-positions
