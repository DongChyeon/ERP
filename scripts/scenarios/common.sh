#!/usr/bin/env bash
set -euo pipefail

EMPLOYEE_HOST=${EMPLOYEE_HOST:-http://localhost:8081}
APPROVAL_REQ_HOST=${APPROVAL_REQ_HOST:-http://localhost:8082}
APPROVAL_PROC_HOST=${APPROVAL_PROC_HOST:-http://localhost:8083}

declare REQUESTER_ID=${REQUESTER_ID:-}
declare APPROVER1_ID=${APPROVER1_ID:-}
declare APPROVER2_ID=${APPROVER2_ID:-}

function create_employee() {
  local name=$1
  local department=$2
  local position=$3

  curl -sS -X POST "$EMPLOYEE_HOST/employees" \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"$name\",\"department\":\"$department\",\"position\":\"$position\"}"
}

function ensure_setup() {
  if [[ -z "$REQUESTER_ID" || -z "$APPROVER1_ID" || -z "$APPROVER2_ID" ]]; then
    REQUESTER_ID=$(create_employee "Requester Kim" "Finance" "Manager" | jq -r '.id')
    APPROVER1_ID=$(create_employee "Approver Lee" "Finance" "Director" | jq -r '.id')
    APPROVER2_ID=$(create_employee "Approver Choi" "Finance" "CFO" | jq -r '.id')
  fi
}

function create_approval() {
  local title=$1
  local content=$2

  curl -sS -X POST "$APPROVAL_REQ_HOST/approvals" \
    -H 'Content-Type: application/json' \
    -d "{\"requesterId\":$REQUESTER_ID,\"title\":\"$title\",\"content\":\"$content\",\"steps\":[{\"step\":1,\"approverId\":$APPROVER1_ID},{\"step\":2,\"approverId\":$APPROVER2_ID}]}" |
    jq -r '.requestId'
}

function approve_request() {
  local approver=$1
  local request_id=$2
  local status=$3
  curl -sS -X POST "$APPROVAL_PROC_HOST/process/$approver/$request_id" \
    -H 'Content-Type: application/json' \
    -d "{\"status\":\"$status\"}"
}
