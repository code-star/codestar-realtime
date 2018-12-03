#!/usr/bin/env bash

# working directory for script
thisDir=`dirname $0`

docker-compose -f ${thisDir}/docker-compose.yml down
