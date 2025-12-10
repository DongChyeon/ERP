#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd $(dirname "$0") && pwd)
source "$SCRIPT_DIR/common.sh"
ensure_setup
request_id=$(create_approval "Concurrent Demo" "Same approver different states")
echo "Created approval ID: $request_id"

echo "Approver1 sends approved and rejected nearly simultaneously"
(approve_request "$APPROVER1_ID" "$request_id" "approved" | jq) &
(approve_request "$APPROVER1_ID" "$request_id" "rejected" | jq) &
wait

echo "Final status after conflicting submissions:"
curl -sS "$APPROVAL_REQ_HOST/approvals/$request_id" | jq
