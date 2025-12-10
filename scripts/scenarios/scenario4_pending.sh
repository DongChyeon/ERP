#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd $(dirname "$0") && pwd)
source "$SCRIPT_DIR/common.sh"
ensure_setup
request_id=$(create_approval "Pending Verification" "Ensure removal")
echo "Created approval ID: $request_id"

echo "Approver1 pending before action:"
curl -sS "$APPROVAL_PROC_HOST/process/$APPROVER1_ID" | jq

echo "Approver2 pending before step1:"
curl -sS "$APPROVAL_PROC_HOST/process/$APPROVER2_ID" | jq

echo "Approver1 approves"
approve_request "$APPROVER1_ID" "$request_id" "approved" | jq

echo "Approver1 pending after approval (should be removed):"
curl -sS "$APPROVAL_PROC_HOST/process/$APPROVER1_ID" | jq
