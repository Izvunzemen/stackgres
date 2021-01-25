#!/bin/sh

LOCK_RESOURCE="cronjob.batch"
LOCK_RESOURCE_NAME="$CRONJOB_NAME"

. "$LOCAL_BIN_SHELL_UTILS_PATH"

run() {
  set -e

  acquire_lock > /tmp/try-lock
  echo "Lock acquired"
  maintain_lock >> /tmp/try-lock &
  TRY_LOCK_PID=$!

  reconcile_backups &
  PID=$!

  set +e
  (
  set +x
  while (kill -0 "$PID" && kill -0 "$TRY_LOCK_PID") 2>/dev/null
  do
    true
  done
  )

  if kill -0 "$PID" 2>/dev/null
  then
    kill_with_childs "$PID"
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Lock lost:\n'"$(cat /tmp/try-lock | to_json_string)"'"}
      ]'
    cat /tmp/try-lock
    echo "Lock lost"
    return 1
  else
    kill_with_childs "$TRY_LOCK_PID"
    wait "$PID"
    EXIT_CODE="$?"
    if [ "$EXIT_CODE" != 0 ]
    then
      cat /tmp/backup-push
      echo "Backup failed"
      [ -n "$SCHEDULED_BACKUP_KEY" ] || sleep 20
      return 1
    fi
  fi
}

reconcile_backups() {
  set -e
  get_backup_crs

  if [ -n "$SCHEDULED_BACKUP_KEY" ]
  then
    BACKUP_NAME="${CLUSTER_NAME}-$(date +%Y-%m-%d-%H-%M-%S --utc)"
  fi

  BACKUP_CONFIG_RESOURCE_VERSION="$(kubectl get "$BACKUP_CONFIG_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CONFIG" --template '{{ .metadata.resourceVersion }}')"
  BACKUP_ALREADY_COMPLETED=false
  create_or_update_backup_cr
  if [ "$BACKUP_ALREADY_COMPLETED" = "true" ]
  then
    echo "Already completed backup. Nothing to do!"
    return
  fi

  CURRENT_BACKUP_CONFIG="$(kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" \
    --template "{{ .status.sgBackupConfig.storage }}")"

  set +e
  echo "Retrieving primary and replica"
  get_primary_and_replica_pods

  echo "Performing backup"
  do_backup
  echo "Backup completed"

  echo "Extracting pg_controldata"
  extract_controldata
  if [ "$?" = 0 ]
  then
    echo "Extraction of pg_controldata completed"
  else
    echo "Extraction of pg_controldata failed"
  fi

  echo "Retain backups"
  retain_backups
  if [ "$?" = 0 ]
  then
    echo "Reconciliation of backups completed"
  else
    echo "Reconciliation of backups failed"
  fi

  echo "Listing existing backups"
  list_backups
  if [ "$?" != 0 ]
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Backup can not be listed after creation '"$(cat /tmp/backup-list | to_json_string)"'"}
      ]'
    cat /tmp/backup-list
    echo "Backups can not be listed after creation"
    return 1
  fi
  cat /tmp/backup-list | tr -d '[]' | sed 's/},{/}|{/g' | tr '|' '\n' \
    | grep '"backup_name":"'"$CURRENT_BACKUP_NAME"'"' | tr -d '{}"' | tr ',' '\n' > /tmp/current-backup
  if [ "$BACKUP_CONFIG_RESOURCE_VERSION" != "$(kubectl get "$BACKUP_CONFIG_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CONFIG" --template '{{ .metadata.resourceVersion }}')" ]
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Backup configuration '"$BACKUP_CONFIG"' changed during backup"}
      ]'
    cat /tmp/backup-list
    echo "Backup configuration '$BACKUP_CONFIG' changed during backup"
    return 1
  elif ! grep -q "^backup_name:${CURRENT_BACKUP_NAME}$" /tmp/current-backup
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Backup '"$CURRENT_BACKUP_NAME"' was not found after creation"}
      ]'
    cat /tmp/backup-list
    echo "Backup '$CURRENT_BACKUP_NAME' was not found after creation"
    return 1
  fi

  echo "Updating backup CR as completed"
  set_backup_completed
  echo "Backup CR updated as completed"

  echo "Reconcile backup CRs"
  reconcile_backup_crs
  echo "Reconciliation of backup CRs completed"
}

get_backup_crs() {
  BACKUP_CR_TEMPLATE="{{ range .items }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}{{ .spec.sgCluster }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ .metadata.name }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ with .status.process.status }}{{ . }}{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ with .status.internalName }}{{ . }}{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ with .status.process.jobPod }}{{ . }}{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ with .metadata.labels }}{{ with index . \"$SCHEDULED_BACKUP_KEY\" }}{{ . }}{{ end }}{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ if .spec.managedLifecycle }}true{{ else }}false{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}:{{ if .status.process.managedLifecycle }}true{{ else }}false{{ end }}"
  BACKUP_CR_TEMPLATE="${BACKUP_CR_TEMPLATE}{{ printf "'"\n"'" }}{{ end }}"
  kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" \
    --template "$BACKUP_CR_TEMPLATE" > /tmp/all-backups
  grep "^$CLUSTER_NAME:" /tmp/all-backups > /tmp/backups || true
}

create_or_update_backup_cr() {
  BACKUP_CONFIG_YAML=$(cat << BACKUP_CONFIG_YAML_EOF
    baseBackups:
      compression: "{{ .spec.baseBackups.compression }}"
    storage:
      type: "{{ .spec.storage.type }}"
      {{- with .spec.storage.s3 }}
      s3:
        bucket: "{{ .bucket }}"
        {{ with .path }}path: "{{ . }}"{{ end }}
        awsCredentials:
          secretKeySelectors:
            accessKeyId:
              key: "{{ .awsCredentials.secretKeySelectors.accessKeyId.key }}"
              name: "{{ .awsCredentials.secretKeySelectors.accessKeyId.name }}"
            secretAccessKey:
              key: "{{ .awsCredentials.secretKeySelectors.secretAccessKey.key }}"
              name: "{{ .awsCredentials.secretKeySelectors.secretAccessKey.name }}"
        {{ with .region }}region: "{{ . }}"{{ end }}
        {{ with .storageClass }}storageClass: "{{ . }}"{{ end }}
      {{- end }}
      {{- with .spec.storage.s3Compatible }}
      s3Compatible:
        bucket: "{{ .bucket }}"
        {{ with .path }}path: "{{ . }}"{{ end }}
        awsCredentials:
          secretKeySelectors:
            accessKeyId:
              key: "{{ .awsCredentials.secretKeySelectors.accessKeyId.key }}"
              name: "{{ .awsCredentials.secretKeySelectors.accessKeyId.name }}"
            secretAccessKey:
              key: "{{ .awsCredentials.secretKeySelectors.secretAccessKey.key }}"
              name: "{{ .awsCredentials.secretKeySelectors.secretAccessKey.name }}"
        {{ with .region }}region: "{{ . }}"{{ end }}
        {{ with .endpoint }}endpoint: "{{ . }}"{{ end }}
        {{ with .enablePathStyleAddressing }}enablePathStyleAddressing: {{ . }}{{ end }}
        {{ with .storageClass }}storageClass: "{{ . }}"{{ end }}
      {{- end }}
      {{- with .spec.storage.gcs }}
      gcs:
        bucket: "{{ .bucket }}"
        {{ with .path }}path: "{{ . }}"{{ end }}
        gcpCredentials:
          {{- if .gcpCredentials.fetchCredentialsFromMetadataService }}
          fetchCredentialsFromMetadataService: true
          {{- else }}
          secretKeySelectors:
            serviceAccountJSON:
              key: "{{ .gcpCredentials.secretKeySelectors.serviceAccountJSON.key }}"
              name: "{{ .gcpCredentials.secretKeySelectors.serviceAccountJSON.name }}"
          {{- end }}
      {{- end }}
      {{- with .spec.storage.azureBlob }}
      azureBlob:
        bucket: "{{ .bucket }}"
        {{ with .path }}path: "{{ . }}"{{ end }}
        azureCredentials:
          secretKeySelectors:
            storageAccount:
              key: "{{ .azureCredentials.secretKeySelectors.storageAccount.key }}"
              name: "{{ .azureCredentials.secretKeySelectors.storageAccount.name }}"
            accessKey:
              key: "{{ .azureCredentials.secretKeySelectors.accessKey.key }}"
              name: "{{ .azureCredentials.secretKeySelectors.accessKey.name }}"
      {{- end }}
BACKUP_CONFIG_YAML_EOF
  )
BACKUP_STATUS_YAML=$(cat << BACKUP_STATUS_YAML_EOF
status:
  process:
    status: "$BACKUP_PHASE_RUNNING"
    jobPod: "$POD_NAME"
  sgBackupConfig:
$(kubectl get "$BACKUP_CONFIG_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CONFIG" --template "$BACKUP_CONFIG_YAML")
BACKUP_STATUS_YAML_EOF
  )

  if ! kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" -o name >/dev/null 2>&1
  then
    echo "Creating backup CR"
    cat << EOF | kubectl create -f - -o yaml
apiVersion: $BACKUP_CRD_APIVERSION
kind: $BACKUP_CRD_KIND
metadata:
  namespace: "$CLUSTER_NAMESPACE"
  name: "$BACKUP_NAME"
  annotations:
    $SCHEDULED_BACKUP_KEY: "$RIGHT_VALUE"
spec:
  sgCluster: "$CLUSTER_NAME"
  managedLifecycle: true
$BACKUP_STATUS_YAML
EOF
  else
    if ! kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --template "{{ .status.process.status }}" \
      | grep -q "^$BACKUP_PHASE_COMPLETED$"
    then
      echo "Updating backup CR"
      kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" -o yaml --type merge --patch "$(
        (
          kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" -o yaml
          echo "$BACKUP_STATUS_YAML"
        ) | kubectl create --dry-run=client -f - -o json)"
    else
      BACKUP_ALREADY_COMPLETED=true
    fi
  fi
}

get_primary_and_replica_pods() {
  kubectl get pod -n "$CLUSTER_NAMESPACE" -l "${PATRONI_CLUSTER_LABELS},${PATRONI_ROLE_KEY}=${PATRONI_PRIMARY_ROLE}" -o name > /tmp/current-primary
  kubectl get pod -n "$CLUSTER_NAMESPACE" -l "${PATRONI_CLUSTER_LABELS},${PATRONI_ROLE_KEY}=${PATRONI_REPLICA_ROLE}" -o name | head -n 1 > /tmp/current-replica-or-primary
  if [ ! -s /tmp/current-primary ]
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Unable to find primary, backup aborted"}
      ]'
    kubectl get pod -n "$CLUSTER_NAMESPACE" -l "${PATRONI_CLUSTER_LABELS}" >&2
    echo > /tmp/backup-push
    echo "Unable to find primary, backup aborted" >> /tmp/backup-push
    exit 1
  fi

  if [ ! -s /tmp/current-replica-or-primary ]
  then
    cat /tmp/current-primary > /tmp/current-replica-or-primary
    echo "Primary is $(cat /tmp/current-primary)"
    echo "Replica not found, primary will be used for cleanups"
  else
    echo "Primary is $(cat /tmp/current-primary)"
    echo "Replica is $(cat /tmp/current-replica-or-primary)"
  fi
}

do_backup() {
  cat << EOF | kubectl exec -i -n "$CLUSTER_NAMESPACE" "$(cat /tmp/current-primary)" -c patroni \
    -- sh -e $SHELL_XTRACE > /tmp/backup-push 2>&1
exec-with-env "$BACKUP_ENV" \\
  -- wal-g backup-push "$PG_DATA_PATH" -f $([ "$BACKUP_IS_PERMANENT" = true ] && echo '-p' || true)
EOF
  if [ "$?" != 0 ]
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Backup failed: '"$(cat /tmp/backup-push | to_json_string)"'"}
      ]'
    exit 1
  fi
  CURRENT_BACKUP_NAME=
  if grep -q " Wrote backup with name " /tmp/backup-push
  then
    CURRENT_BACKUP_NAME="$(grep " Wrote backup with name " /tmp/backup-push | sed 's/.* \([^ ]\+\)$/\1/')"
  fi
  if [ -z "$CURRENT_BACKUP_NAME" ]
  then
    kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch '[
      {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_FAILED"'"},
      {"op":"replace","path":"/status/process/failure","value":"Backup name not found in backup-push log:\n'"$(cat /tmp/backup-push | to_json_string)"'"}
      ]'
    cat /tmp/backup-push
    echo "Backup name not found in backup-push log"
    exit 1
  fi
}

extract_controldata() {
  cat << EOF | kubectl exec -i -n "$CLUSTER_NAMESPACE" "$(cat /tmp/current-primary)" -c patroni \
      -- sh -e $SHELL_XTRACE > /tmp/pg_controldata
pg_controldata --pgdata="$PG_DATA_PATH"
EOF
  if [ "$?" = 0 ]
  then
    cat /tmp/pg_controldata | awk -F ':' '{ printf "%s: %s\n", $1, $2 }' | awk '{ $2=$2;print }'| awk -F ': ' '
          BEGIN { print "\n            {"}
          {
            if (NR > 1)
              printf ",\n             \"%s\": \"%s\"", $1, $2
            else
              printf "             \"%s\": \"%s\"", $1, $2
          }
          END { print "\n            }" }' > /tmp/json_controldata
  else
    echo '{}' > /tmp/json_controldata
    return 1
  fi
}

retain_backups() {
  cat << EOF | kubectl exec -i -n "$CLUSTER_NAMESPACE" "$(cat /tmp/current-replica-or-primary)" -c patroni \
  -- sh -e $SHELL_XTRACE
# for each existing backup sorted by backup name ascending (this also mean sorted by creation date ascending)
exec-with-env "$BACKUP_ENV" \\
  -- wal-g backup-list --detail --json \\
  | tr -d '[]' | sed 's/},{/}|{/g' | tr '|' '\\n' \\
  | grep '"backup_name"' \\
  | sort -r -t , -k 2 \\
  | (RETAIN="$RETAIN"
    while read BACKUP
    do
      BACKUP_NAME="\$(echo "\$BACKUP" | tr -d '{}\\42' | tr ',' '\\n' \\
          | grep 'backup_name' | cut -d : -f 2-)"
      echo "Check if backup \$BACKUP_NAME has to be retained and will retain \$RETAIN backups"
      # if is not the created backup and is not in backup CR list, mark as impermanent
      if [ "\$BACKUP_NAME" != "$CURRENT_BACKUP_NAME" ] \\
        && ! echo '$(cat /tmp/backups)' \\
        | cut -d : -f 4 \\
        | grep -v '^\$' \\
        | grep -q "^\$BACKUP_NAME\$"
      then
        if echo "\$BACKUP" | grep -q "\\"is_permanent\\":true"
        then
          echo "Mark \$BACKUP_NAME as impermanent and will retain \$RETAIN backups"
          exec-with-env "$BACKUP_ENV" \\
            -- wal-g backup-mark -i "\$BACKUP_NAME"
        fi
      # if is inside the retain window, mark as permanent and decrease RETAIN counter
      elif [ "\$RETAIN" -gt 0 ]
      then
        if [ "\$BACKUP_NAME" = "$CURRENT_BACKUP_NAME" -a "$BACKUP_IS_PERMANENT" != true ] \\
          || echo "\$BACKUP" | grep -q "\\"is_permanent\\":false"
        then
          echo "Mark \$BACKUP_NAME as permanent and will retain \$((RETAIN-1)) more backups"
          exec-with-env "$BACKUP_ENV" \\
            -- wal-g backup-mark "\$BACKUP_NAME"
        fi
        RETAIN="\$((RETAIN-1))"
      # if is outside the retain window...
      elif [ "\$RETAIN" -le 0 ]
      then
        # ... and has a managed lifecycle, mark as impermanent
        if echo '$(cat /tmp/backups)' \\
          | grep '^[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:true' \\
          | cut -d : -f 4 \\
          | grep -v '^\$' \\
          | grep -q "^\$BACKUP_NAME\$" \\
          && echo "\$BACKUP" | grep -q "\\"is_permanent\\":true"
        then
          echo "Mark \$BACKUP_NAME as impermanent"
          exec-with-env "$BACKUP_ENV" \\
            -- wal-g backup-mark -i "\$BACKUP_NAME"
        # ... and has not a managed lifecycle, mark as permanent
        elif echo '$(cat /tmp/backups)' \\
          | grep -v '^[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:true' \\
          | cut -d : -f 4 \\
          | grep -v '^\$' \\
          | grep -q "^\$BACKUP_NAME\$" \\
          && echo "\$BACKUP" | grep -q "\\"is_permanent\\":false"
        then
          echo "Mark \$BACKUP_NAME as permanent"
          exec-with-env "$BACKUP_ENV" \\
            -- wal-g backup-mark "\$BACKUP_NAME"
        fi
      fi
    done)

# removes all backups that are marked as impermanent
echo "Cleaning up impermanent backups"
exec-with-env "$BACKUP_ENV" \\
  -- wal-g delete retain FIND_FULL "0" --confirm

# for each existing backup
exec-with-env "$BACKUP_ENV" \\
  -- wal-g backup-list --detail --json \\
  | tr -d '[]' | sed 's/},{/}|{/g' | tr '|' '\\n' \\
  | grep '"backup_name"' \\
  | while read BACKUP
    do
      BACKUP_NAME="\$(echo "\$BACKUP" | tr -d '{}\\42' | tr ',' '\\n' \\
          | grep 'backup_name' | cut -d : -f 2-)"
      echo "Check if backup \$BACKUP_NAME has to be set permanent or impermanent"
      # if is the created backup and has a managed lifecycle, mark as impermanent
      if [ "\$BACKUP_NAME" = "$CURRENT_BACKUP_NAME" -a "$BACKUP_IS_PERMANENT" != true ] \\
        || (echo '$(cat /tmp/backups)' \\
        | grep '^[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:true' \\
        | cut -d : -f 4 \\
        | grep -v '^\$' \\
        | grep -q "^\$BACKUP_NAME\$" \\
        && echo "\$BACKUP" | grep -q "\\"is_permanent\\":true")
      then
        echo "Mark \$BACKUP_NAME as impermanent"
        exec-with-env "$BACKUP_ENV" \\
          -- wal-g backup-mark -i "\$BACKUP_NAME"
      # if has not a managed lifecycle, mark as permanent
      elif echo '$(cat /tmp/backups)' \\
        | grep -v '^[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:true' \\
        | cut -d : -f 4 \\
        | grep -v '^\$' \\
        | grep -q "^\$BACKUP_NAME\$" \\
        && echo "\$BACKUP" | grep -q "\\"is_permanent\\":false"
      then
        echo "Mark \$BACKUP_NAME as permanent"
        exec-with-env "$BACKUP_ENV" \\
          -- wal-g backup-mark "\$BACKUP_NAME"
      fi
    done
EOF
}

list_backups() {
  cat << EOF | kubectl exec -i -n "$CLUSTER_NAMESPACE" "$(cat /tmp/current-replica-or-primary)" -c patroni \
    -- sh -e $SHELL_XTRACE > /tmp/backup-list
WALG_LOG_LEVEL= exec-with-env "$BACKUP_ENV" \\
-- wal-g backup-list --detail --json
EOF
  cat /tmp/backup-list | tr -d '[]' | sed 's/},{/}|{/g' | tr '|' '\n' \
    | grep '"backup_name"' \
    > /tmp/existing-backups
}

set_backup_completed() {
  EXISTING_BACKUP_IS_PERMANENT="$(grep "^is_permanent:" /tmp/current-backup | cut -d : -f 2-)"
  IS_BACKUP_SUBJECT_TO_RETENTION_POLICY=""
  if [ "$EXISTING_BACKUP_IS_PERMANENT" = "true" ]
  then
    IS_BACKUP_SUBJECT_TO_RETENTION_POLICY="false"
  else
    IS_BACKUP_SUBJECT_TO_RETENTION_POLICY="true"
  fi

  BACKUP_PATCH='[
    {"op":"replace","path":"/status/internalName","value":"'"$CURRENT_BACKUP_NAME"'"},
    {"op":"replace","path":"/status/process/status","value":"'"$BACKUP_PHASE_COMPLETED"'"},
    {"op":"replace","path":"/status/process/failure","value":""},
    {"op":"replace","path":"/status/process/managedLifecycle","value":'$IS_BACKUP_SUBJECT_TO_RETENTION_POLICY'},
    {"op":"replace","path":"/status/process/timing","value":{
        "stored":"'"$(grep "^time:" /tmp/current-backup | cut -d : -f 2-)"'",
        "start":"'"$(grep "^start_time:" /tmp/current-backup | cut -d : -f 2-)"'",
        "end":"'"$(grep "^finish_time:" /tmp/current-backup | cut -d : -f 2-)"'"
      }
    },
    {"op":"replace","path":"/status/backupInformation","value":{
        "startWalFile":"'"$(grep "^wal_file_name:" /tmp/current-backup | cut -d : -f 2-)"'",
        "timeline":"'"$(grep "^wal_file_name:" /tmp/current-backup | cut -d : -f 2- | awk '{startWal=substr($0, 0, 9); timeline=startWal+0; print timeline}')"'",
        "hostname":"'"$(grep "^hostname:" /tmp/current-backup | cut -d : -f 2-)"'",
        "sourcePod":"'"$(grep "^hostname:" /tmp/current-backup | cut -d : -f 2-)"'",
        "pgData":"'"$(grep "^data_dir:" /tmp/current-backup | cut -d : -f 2-)"'",
        "postgresVersion":"'"$(grep "^pg_version:" /tmp/current-backup | cut -d : -f 2-)"'",
        "systemIdentifier":"'"$(grep "^system_identifier:" /tmp/current-backup | cut -d : -f 2-)"'",
        "lsn":{
          "start":"'"$(grep "^start_lsn:" /tmp/current-backup | cut -d : -f 2-)"'",
          "end":"'"$(grep "^finish_lsn:" /tmp/current-backup | cut -d : -f 2-)"'"
        },
        "size":{
          "uncompressed":'"$(grep "^uncompressed_size:" /tmp/current-backup | cut -d : -f 2-)"',
          "compressed":'"$(grep "^compressed_size:" /tmp/current-backup | cut -d : -f 2-)"'
        },
        "controlData": '"$(cat /tmp/json_controldata)"'
      }
    }
  ]'
  kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_NAME" --type json --patch "$BACKUP_PATCH"
}

reconcile_backup_crs() {
  kubectl get pod -n "$CLUSTER_NAMESPACE" \
    --template "{{ range .items }}{{ .metadata.name }}{{ printf "'"\n"'" }}{{ end }}" \
    > /tmp/pods
  for BACKUP in $(cat /tmp/backups)
  do
    BACKUP_CR_NAME="$(echo "$BACKUP" | cut -d : -f 2)"
    BACKUP_PHASE="$(echo "$BACKUP" | cut -d : -f 3)"
    BACKUP_NAME="$(echo "$BACKUP" | cut -d : -f 4)"
    BACKUP_POD="$(echo "$BACKUP" | cut -d : -f 5)"
    BACKUP_SHEDULED_BACKUP="$(echo "$BACKUP" | cut -d : -f 6)"
    BACKUP_MANAGED_LIFECYCLE="$(echo "$BACKUP" | cut -d : -f 8)"
    BACKUP_IS_PERMANENT="$([ "$BACKUP_MANAGED_LIFECYCLE" = true ] && echo false || echo true)"
    BACKUP_CONFIG="$(kubectl get "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CR_NAME" \
      --template "{{ .status.sgBackupConfig.storage }}")"
    # if backup CR has backup internal name, is marked as completed, uses the same current
    # backup config but is not found in the storage, delete it
    if [ -n "$BACKUP_NAME" ] && [ "$BACKUP_PHASE" = "$BACKUP_PHASE_COMPLETED" ] \
      && [ "$BACKUP_CONFIG" = "$CURRENT_BACKUP_CONFIG" ] \
      && ! grep -q "\"backup_name\":\"$BACKUP_NAME\"" /tmp/existing-backups
    then
      echo "Deleting backup CR $BACKUP_CR_NAME since backup does not exists"
      kubectl delete "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CR_NAME"
    # if backup CR is a scheduled backup, is marked as running, has no pod or pod
    # has been terminated, delete it
    elif [ "$BACKUP_SHEDULED_BACKUP" = "$RIGHT_VALUE" ] \
      && [ "$BACKUP_PHASE" = "$BACKUP_PHASE_RUNNING" ] \
      && ([ -z "$BACKUP_POD" ] || ! grep -q "^$BACKUP_POD$" /tmp/pods)
    then
      echo "Deleting backup CR $BACKUP_CR_NAME since backup is running but pod does not exists"
      kubectl delete "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CR_NAME"
    # if backup CR has backup internal name, is marked as completed, and is marked as
    # stored as not managed lifecycle or managed lifecycle and is found as managed lifecycle or
    # not managed lifecycle respectively, then mark it as stored as managed lifecycle or
    # not managed lifecycle respectively
    elif [ -n "$BACKUP_NAME" ] && [ "$BACKUP_PHASE" = "$BACKUP_PHASE_COMPLETED" ] \
      && ! grep "\"backup_name\":\"$BACKUP_NAME\"" /tmp/existing-backups \
        | grep -q "\"is_permanent\":$BACKUP_IS_PERMANENT"
    then
      EXISTING_BACKUP_IS_PERMANENT="$(grep "\"backup_name\":\"$BACKUP_NAME\"" /tmp/existing-backups \
        | tr -d '{}"' | tr ',' '\n' | grep "^is_permanent:" | cut -d : -f 2-)"
      IS_BACKUP_SUBJECT_TO_RETENTION_POLICY=""
      if [ "$EXISTING_BACKUP_IS_PERMANENT" = "true" ]
      then
        IS_BACKUP_SUBJECT_TO_RETENTION_POLICY="false"
      else
        IS_BACKUP_SUBJECT_TO_RETENTION_POLICY="true"
      fi
      echo "Updating backup CR $BACKUP_CR_NAME .status.process.managedLifecycle to $IS_BACKUP_SUBJECT_TO_RETENTION_POLICY since was updated in the backup"
      kubectl patch "$BACKUP_CRD_NAME" -n "$CLUSTER_NAMESPACE" "$BACKUP_CR_NAME" --type json --patch '[
        {"op":"replace","path":"/status/process/managedLifecycle","value":'$IS_BACKUP_SUBJECT_TO_RETENTION_POLICY'}
        ]'
    fi
  done
}

run
