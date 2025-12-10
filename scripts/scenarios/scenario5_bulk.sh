#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd $(dirname "$0") && pwd)
source "$SCRIPT_DIR/common.sh"
ensure_setup
COUNT=${1:-100}
for i in $(seq 1 "$COUNT"); do
  request_id=$(create_approval "Load Test $i" "Scenario $i")
  echo "Submitted approval $i -> ID $request_id"
done
echo "Submitted $COUNT approvals"
