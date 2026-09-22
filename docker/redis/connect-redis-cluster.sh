#!/usr/bin/env bash
set -eu

function log {
  echo "[$(date)]: $*"
}

nodes=(
  kelarus-platform-redis:6379
  kelarus-platform-redis-1:6379
  kelarus-platform-redis-2:6379
  kelarus-platform-redis-3:6379
  kelarus-platform-redis-4:6379
  kelarus-platform-redis-5:6379
)

if redis-cli -h kelarus-platform-redis cluster info | grep -q 'cluster_state:ok'; then
  log "Redis Cluster already initialized"
  exit 0
fi

log "Create Redis Cluster"
redis-cli --cluster create "${nodes[@]}" --cluster-replicas 1 --cluster-yes
