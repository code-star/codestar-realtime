#!/usr/bin/env bash

# working directory for script
thisDir=`dirname $0`

# Check if jq exists
which -s jq || { >&2 echo 'Missing requirement: jq (OS X: brew install jq)' ; exit 1; }

docker-compose -f ${thisDir}/docker-compose.yml up -d

printf "Waiting for Kafka"
while [[ $SCHEMA != '_schemas' ]]
do
    printf "."
    sleep 1;
    SCHEMA=`curl -s http://localhost:8082/topics | jq -r '.[1]'`
done
echo

echo "Kafka Started"

${thisDir}/createTopics.sh
