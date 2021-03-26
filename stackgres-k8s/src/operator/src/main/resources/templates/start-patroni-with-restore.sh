export HOME="$PG_BASE_PATH"
export PATRONI_POSTGRESQL_LISTEN="$(eval "echo $PATRONI_POSTGRESQL_LISTEN")"
export PATRONI_POSTGRESQL_CONNECT_ADDRESS="$(eval "echo $PATRONI_POSTGRESQL_CONNECT_ADDRESS")"

cat << 'EOF' | exec-with-env "${RESTORE_ENV}" -- sh -ex
if [ -n "$ENDPOINT_HOSTNAME" ] && [ -n "$ENDPOINT_PORT" ]
then
  if cat < /dev/null > "/dev/tcp/$ENDPOINT_HOSTNAME/$ENDPOINT_PORT"
  then
    echo "Host $ENDPOINT_HOSTNAME:$ENDPOINT_PORT reachable"
  else
    echo "ERROR: Host $ENDPOINT_HOSTNAME:$ENDPOINT_PORT not reachable"
    exit 1
  fi
fi
EOF

cat << EOF > "$PATRONI_CONFIG_PATH/postgres.yml"
scope: ${PATRONI_SCOPE}
name: ${PATRONI_NAME}

bootstrap:
  post_init: '${LOCAL_BIN_PATH}/post-init.sh'
  method: wal_g
  wal_g:
    command: '${PATRONI_CONFIG_PATH}/bootstrap'
    keep_existing_recovery_conf: False
    recovery_conf:
      restore_command: 'exec-with-env "${RESTORE_ENV}" -- wal-g wal-fetch %f %p'
      recovery_target_timeline: 'latest'
      recovery_target_action: 'promote'
  initdb:
  - auth-host: md5
  - auth-local: trust
  - encoding: UTF8
  - locale: C.UTF-8
  - data-checksums
  pg_hba:
  - 'host all all 0.0.0.0/0 md5'
  - 'host replication ${PATRONI_REPLICATION_USERNAME} 0.0.0.0/0 md5'
restapi:
  connect_address: '${PATRONI_KUBERNETES_POD_IP}:8008'
  listen: 0.0.0.0:8008
postgresql:
  use_pg_rewind: true
  remove_data_directory_on_rewind_failure: true
  use_unix_socket: true
  connect_address: '${PATRONI_KUBERNETES_POD_IP}:5432'
  listen: 0.0.0.0:5432
  authentication:
    superuser:
      password: '${PATRONI_SUPERUSER_PASSWORD}'
    replication:
      password: '${PATRONI_REPLICATION_PASSWORD}'
  parameters:
    unix_socket_directories: '${PATRONI_POSTGRES_UNIX_SOCKET_DIRECTORY}'
  basebackup:
    checkpoint: 'fast'
watchdog:
  mode: off
EOF

cat << EOF > "$PATRONI_CONFIG_PATH/bootstrap"
#!/bin/sh

exec-with-env "$RESTORE_ENV" \\
  -- sh -ec 'wal-g backup-fetch "\$PG_DATA_PATH" "\$RESTORE_BACKUP_ID"'
EOF
chmod a+x "$PATRONI_CONFIG_PATH/bootstrap"

export LC_ALL=C.UTF-8

unset PATRONI_SUPERUSER_PASSWORD PATRONI_REPLICATION_PASSWORD

exec /usr/bin/patroni "$PATRONI_CONFIG_PATH/postgres.yml"
