#!/bin/bash
TYPE="$1"
OBJECT="$2"
SECRET="${STRIPE_WEBHOOK_SECRET:?set STRIPE_WEBHOOK_SECRET}"
URL="${URL:-http://localhost:8080/api/v1/payments/webhook}"
EVENT_ID="${EVENT_ID:-evt_local_$(date +%s%N)}"
PAYLOAD="{\"id\":\"$EVENT_ID\",\"object\":\"event\",\"type\":\"$TYPE\",\"data\":{\"object\":$OBJECT}}"
TS=$(date +%s)
SIG=$(printf '%s.%s' "$TS" "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | sed 's/^.* //')
curl -s -i -X POST "$URL" -H "Content-Type: application/json" -H "Stripe-Signature: t=$TS,v1=$SIG" -d "$PAYLOAD"
