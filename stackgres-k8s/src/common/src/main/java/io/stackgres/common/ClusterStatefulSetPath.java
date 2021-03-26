/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import org.jooq.lambda.Seq;

public enum ClusterStatefulSetPath implements VolumePath {

  ETC_PASSWD_PATH("/etc/passwd"),
  ETC_GROUP_PATH("/etc/group"),
  ETC_SHADOW_PATH("/etc/shadow"),
  ETC_GSHADOW_PATH("/etc/gshadow"),
  SHARED_MEMORY_PATH("/dev/shm"),
  LOCAL_BIN_PATH("/usr/local/bin"),
  LOCAL_BIN_SHELL_UTILS_PATH(LOCAL_BIN_PATH, "shell-utils"),
  LOCAL_BIN_SETUP_DATA_PATHS_SH_PATH(LOCAL_BIN_PATH, "setup-data-paths.sh"),
  LOCAL_BIN_SETUP_ARBITRARY_USER_SH_PATH(LOCAL_BIN_PATH, "setup-arbitrary-user.sh"),
  LOCAL_BIN_SETUP_SCRIPTS_SH_PATH(LOCAL_BIN_PATH, "setup-scripts.sh"),
  LOCAL_BIN_START_PATRONI_SH_PATH(LOCAL_BIN_PATH, "start-patroni.sh"),
  LOCAL_BIN_START_PATRONI_WITH_RESTORE_SH_PATH(LOCAL_BIN_PATH, "start-patroni-with-restore.sh"),
  LOCAL_BIN_POST_INIT_SH_PATH(LOCAL_BIN_PATH, "post-init.sh"),
  LOCAL_BIN_EXEC_WITH_ENV_PATH(LOCAL_BIN_PATH, "exec-with-env"),
  LOCAL_BIN_CREATE_BACKUP_SH_PATH(LOCAL_BIN_PATH, "create-backup.sh"),
  LOCAL_BIN_SET_DBOPS_RUNNING_SH_PATH(LOCAL_BIN_PATH, "set-dbops-running.sh"),
  LOCAL_BIN_RUN_DBOPS_SH_PATH(LOCAL_BIN_PATH, "run-dbops.sh"),
  LOCAL_BIN_SET_DBOPS_RESULT_SH_PATH(LOCAL_BIN_PATH, "set-dbops-result.sh"),
  LOCAL_BIN_RUN_PGBENCH_SH_PATH(LOCAL_BIN_PATH, "dbops/pgbench/run-pgbench.sh"),
  LOCAL_BIN_SET_PGBENCH_RESULT_SH_PATH(LOCAL_BIN_PATH, "dbops/pgbench/set-pgbench-result.sh"),
  LOCAL_BIN_RUN_VACUUM_SH_PATH(LOCAL_BIN_PATH, "dbops/vacuum/run-vacuum.sh"),
  LOCAL_BIN_RUN_REPACK_SH_PATH(LOCAL_BIN_PATH, "dbops/repack/run-repack.sh"),
  LOCAL_BIN_RUN_MAJOR_VERSION_UPGRADE_SH_PATH(LOCAL_BIN_PATH,
      "dbops/major-version-upgrade/run-major-version-upgrade.sh"),
  LOCAL_BIN_RUN_RESTART_SH_PATH(LOCAL_BIN_PATH,
      "dbops/restart/run-restart.sh"),
  LOCAL_BIN_COPY_BINARIES_SH_PATH(LOCAL_BIN_PATH,
      "dbops/major-version-upgrade/copy-binaries.sh"),
  LOCAL_BIN_MAJOR_VERSION_UPGRADE_SH_PATH(LOCAL_BIN_PATH,
      "dbops/major-version-upgrade/major-version-upgrade.sh"),
  LOCAL_BIN_RESET_PATRONI_INITIALIZE_SH_PATH(LOCAL_BIN_PATH,
      "dbops/major-version-upgrade/reset-patroni-initialize.sh"),
  PG_BASE_PATH("/var/lib/postgresql"),
  PG_DATA_PATH(PG_BASE_PATH, "data"),
  PG_EXTENSIONS_PATH(PG_BASE_PATH, "extensions"),
  PG_UPGRADE_PATH(PG_BASE_PATH, "upgrade"),
  PG_RUN_PATH("/var/run/postgresql"),
  PG_LOG_PATH("/var/log/postgresql"),
  BASE_ENV_PATH("/etc/env"),
  BASE_SECRET_PATH(BASE_ENV_PATH, ".secret"),
  PATRONI_ENV_PATH(BASE_ENV_PATH, ClusterStatefulSetEnvVars.PATRONI_ENV.value()),
  PATRONI_CONFIG_PATH("/etc/patroni"),
  BACKUP_ENV_PATH(BASE_ENV_PATH, ClusterStatefulSetEnvVars.BACKUP_ENV.value()),
  BACKUP_SECRET_PATH(BASE_SECRET_PATH, ClusterStatefulSetEnvVars.BACKUP_ENV.value()),
  RESTORE_ENV_PATH(BASE_ENV_PATH, ClusterStatefulSetEnvVars.RESTORE_ENV.value()),
  RESTORE_SECRET_PATH(BASE_SECRET_PATH, ClusterStatefulSetEnvVars.RESTORE_ENV.value()),
  TEMPLATES_PATH("/templates"),
  SHARED_PATH("/shared");

  private final String path;
  private final String subPath;
  private final EnvVar envVar;

  ClusterStatefulSetPath(String path) {
    this.path = path;
    this.subPath = path.substring(1);
    this.envVar = new EnvVarBuilder()
        .withName(name())
        .withValue(path)
        .build();
  }

  ClusterStatefulSetPath(ClusterStatefulSetPath parent, String...paths) {
    this(Seq.of(parent.path).append(paths).toString("/"));
  }

  @Override
  public String filename() {
    int indexOfLastSlash = path.lastIndexOf('/');
    return indexOfLastSlash != -1 ? path.substring(indexOfLastSlash + 1) : path;
  }

  @Override
  public String path() {
    return path;
  }

  @Override
  public String subPath() {
    return subPath;
  }

  public EnvVar envVar() {
    return envVar;
  }
}
