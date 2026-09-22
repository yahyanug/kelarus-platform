#!/bin/sh

set -eu

log() {
  echo "[$(date)] $*"
}

FIRST_NODE="kelarus-platform-redis-cluster-1"

NODES="
kelarus-platform-redis-cluster-1:6379
kelarus-platform-redis-cluster-2:6379
kelarus-platform-redis-cluster-3:6379
kelarus-platform-redis-cluster-4:6379
kelarus-platform-redis-cluster-5:6379
kelarus-platform-redis-cluster-6:6379
"

if redis-cli -h "$FIRST_NODE" cluster info 2>/dev/null \
  | grep -q "cluster_state:ok"; then
  log "Redis Cluster already initialized"
  exit 0
fi

log "Creating Redis Cluster"

redis-cli --cluster create \
  $NODES \
  --cluster-replicas 1 \
  --cluster-yes

log "Redis Cluster created"