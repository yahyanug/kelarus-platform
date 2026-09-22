#!/bin/sh

set -eu

sed -i \
  -e "s|\$SENTINEL_MASTER_NAME|${SENTINEL_MASTER_NAME}|g" \
  -e "s|\$SENTINEL_MASTER_HOST|${SENTINEL_MASTER_HOST}|g" \
  -e "s|\$SENTINEL_MASTER_PORT|${SENTINEL_MASTER_PORT}|g" \
  -e "s|\$SENTINEL_QUORUM|${SENTINEL_QUORUM}|g" \
  -e "s|\$SENTINEL_DOWN_AFTER|${SENTINEL_DOWN_AFTER}|g" \
  -e "s|\$SENTINEL_FAILOVER|${SENTINEL_FAILOVER}|g" \
  /redis/sentinel.conf

exec redis-server /redis/sentinel.conf --sentinel