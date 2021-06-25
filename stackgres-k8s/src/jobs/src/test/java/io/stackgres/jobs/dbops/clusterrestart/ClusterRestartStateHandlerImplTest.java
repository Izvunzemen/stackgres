/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops.clusterrestart;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.quarkus.test.junit.QuarkusTest;
import io.stackgres.common.crd.sgcluster.ClusterDbOpsRestartStatus;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsRestartStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterDbOpsStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterStatus;
import io.stackgres.common.crd.sgdbops.DbOpsRestartStatus;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsRestartStatus;
import io.stackgres.jobs.dbops.AbstractRestartStateHandler;
import io.stackgres.jobs.dbops.ClusterStateHandlerTest;
import io.stackgres.jobs.dbops.StateHandler;

@QuarkusTest
class ClusterRestartStateHandlerImplTest extends ClusterStateHandlerTest {

  @Inject
  @StateHandler("restart")
  ClusterRestartStateHandlerImpl restartStateHandler;

  @Override
  public AbstractRestartStateHandler getRestartStateHandler() {
    return restartStateHandler;
  }

  @Override
  public DbOpsRestartStatus getRestartStatus(StackGresDbOps dbOps) {
    return dbOps.getStatus().getRestart();
  }

  @Override
  public Optional<ClusterDbOpsRestartStatus> getRestartStatus(StackGresCluster dbOps) {
    return Optional.ofNullable(dbOps)
        .map(StackGresCluster::getStatus)
        .map(StackGresClusterStatus::getDbOps)
        .map(StackGresClusterDbOpsStatus::getRestart);
  }

  @Override
  protected void initializeDbOpsStatus(StackGresDbOps dbOps, List<Pod> pods) {
    final StackGresDbOpsRestartStatus restartStatus = new StackGresDbOpsRestartStatus();
    restartStatus.setInitialInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList())
    );
    restartStatus.setPrimaryInstance(getPrimaryInstance(pods).getMetadata().getName());
    restartStatus.setPendingToRestartInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList())
    );
    restartStatus.setSwitchoverInitiated(Boolean.FALSE.toString());

    dbOps.getStatus().setRestart(restartStatus);
  }

  @Override
  protected void initializeClusterStatus(StackGresCluster cluster, List<Pod> pods) {

    final StackGresClusterStatus status = new StackGresClusterStatus();
    final StackGresClusterDbOpsStatus dbOps = new StackGresClusterDbOpsStatus();
    final StackGresClusterDbOpsRestartStatus restartStatus = new StackGresClusterDbOpsRestartStatus();
    restartStatus.setInitialInstances(
        pods.stream()
            .map(Pod::getMetadata).map(ObjectMeta::getName)
            .collect(Collectors.toList())
    );
    restartStatus.setPrimaryInstance(getPrimaryInstance(pods).getMetadata().getName());
    dbOps.setRestart(restartStatus);
    status.setDbOps(dbOps);
    cluster.setStatus(status);
  }
}