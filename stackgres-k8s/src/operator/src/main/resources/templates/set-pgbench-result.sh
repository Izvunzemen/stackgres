#!/bin/sh

set_completed() {
  SCALE_FACTOR="$(grep '^\s*scaling factor: ' "$SHARED_PATH/$KEBAB_OP_NAME.out" | sed 's/\s\+//g' | cut -d : -f 2 \
    | grep -v '^$' || echo null)"
  TRANSACTION_PROCESSED="$(grep '^\s*number of transactions actually processed: ' "$SHARED_PATH/$KEBAB_OP_NAME.out" \
    | sed 's/\s\+//g' | cut -d : -f 2 | cut -d / -f 1 | grep -v '^$' || echo null)"
  LATENCY_AVERAGE="$(grep '^\s*latency average = ' "$SHARED_PATH/$KEBAB_OP_NAME.out" \
    | sed 's/\s\+//g' | cut -d = -f 2 | sed 's/[^0-9.]\+$//' | grep -v '^$' | format_measure || echo null)"
  LATENCY_STDDEV="$(grep '^\s*latency stddev = ' "$SHARED_PATH/$KEBAB_OP_NAME.out" \
    | sed 's/\s\+//g' | cut -d = -f 2 | sed 's/[^0-9.]\+$//' | grep -v '^$' | format_measure || echo null)"
  TPS_INCLUDING_CONNECTIONS_ESTABLISHING="$(grep '^\s*tps = ' "$SHARED_PATH/$KEBAB_OP_NAME.out" \
    | grep '(including connections establishing)$' | sed 's/\s\+//g' | cut -d = -f 2 | cut -d '(' -f 1 \
    | grep -v '^$' | format_measure || echo null)"
  TPS_EXCLUDING_CONNECTIONS_ESTABLISHING="$(grep '^\s*tps = ' "$SHARED_PATH/$KEBAB_OP_NAME.out" \
    | grep '(excluding connections establishing)$' | sed 's/\s\+//g' | cut -d = -f 2 | cut -d '(' -f 1 \
    | grep -v '^$' | format_measure || echo null)"
  kubectl patch "$DB_OPS_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$DB_OPS_NAME" --type=json \
    -p "$(cat << EOF
[
  {"op":"replace","path":"/status/conditions","value":[
      $(eval_in_place "$CONDITION_DB_OPS_FALSE_RUNNING"),
      $(eval_in_place "$CONDITION_DB_OPS_COMPLETED"),
      $(eval_in_place "$CONDITION_DB_OPS_FALSE_FAILED")
    ]
  },
  {"op":"replace","path":"/status/benchmark","value":{
      "pgbench": {
        "scaleFactor": $SCALE_FACTOR,
        "transactionsProcessed": $TRANSACTION_PROCESSED,
        "latency": {
          "average": {
            "value": $LATENCY_AVERAGE,
            "unit": "ms"
          },
          "standardDeviation": {
            "value": $LATENCY_STDDEV,
            "unit": "ms"
          }
        },
        "transactionsPerSecond": {
          "includingConnectionsEstablishing": {
            "value": $TPS_INCLUDING_CONNECTIONS_ESTABLISHING,
            "unit": "tps"
          },
          "excludingConnectionsEstablishing": {
            "value": $TPS_EXCLUDING_CONNECTIONS_ESTABLISHING,
            "unit": "tps"
          }
        }
      }
    }
  }
]
EOF
    )"
}

format_measure() {
  read INPUT
  LC_NUMERIC="en_US.UTF-8" printf '%.2f' $INPUT
}

set_completed