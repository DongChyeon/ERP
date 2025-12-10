#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd $(dirname "$0") && pwd)
source "$SCRIPT_DIR/common.sh"
ensure_setup

WS_HOST=${WS_HOST:-ws://localhost:8084}
LOG_FILE=$(mktemp)
echo "Connecting WebSocket via wscat (logs -> $LOG_FILE)"
wscat -c "$WS_HOST/ws?id=$REQUESTER_ID" 2>&1 | tee "$LOG_FILE" &
WS_PID=$!
sleep 1

request_id=$(create_approval "Annual Budget" "FY plan")
echo "Created approval ID: $request_id"
approve_request "$APPROVER1_ID" "$request_id" "approved" | jq
approve_request "$APPROVER2_ID" "$request_id" "approved" | jq
sleep 1

if kill -0 "$WS_PID" 2>/dev/null; then
  kill "$WS_PID" >/dev/null 2>&1 || true
else
  echo "WebSocket client already exited"
fi

echo "Final status:"
curl -sS "$APPROVAL_REQ_HOST/approvals/$request_id" | jq
