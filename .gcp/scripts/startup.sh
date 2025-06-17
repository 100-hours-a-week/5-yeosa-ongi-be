#!/bin/bash
docker run -d \
  --name node_exporter \
  --net=host \
  --pid=host \
  --restart unless-stopped \
  prom/node-exporter
