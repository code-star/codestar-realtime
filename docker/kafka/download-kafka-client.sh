#!/usr/bin/env bash

wget -qO- http://apache.40b.nl/kafka/2.1.0/kafka_2.12-2.1.0.tgz | tar xvz -C $(dirname $0)
