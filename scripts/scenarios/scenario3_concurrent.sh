#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd $(dirname "$0") && pwd)
source "$SCRIPT_DIR/common.sh"
ensure_setup
request_id=$(create_approval "Concurrent Demo" "Simultaneous approvals")
echo "Created approval ID: $request_id"
approve_request "$APPROVER1_ID" "$request_id" "approved" &
approve_request "$APPROVER2_ID" "$request_id" "approved" &
wait
echo "Duplicate submit from approver2 (should be idempotent)"
approve_request "$APPROVER2_ID" "$request_id" "approved" || true
echo "Final status after concurrent approvals:"
curl -sS "$APPROVAL_REQ_HOST/approvals/$request_id" | jq
