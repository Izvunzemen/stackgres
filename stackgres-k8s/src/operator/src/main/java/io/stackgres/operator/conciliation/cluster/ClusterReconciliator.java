/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.cluster;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.stackgres.common.crd.sgcluster.ClusterEventReason;
import io.stackgres.common.crd.sgcluster.ClusterStatusCondition;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterCondition;
import io.stackgres.common.event.EventEmitter;
import io.stackgres.common.event.EventEmitterType;
import io.stackgres.common.resource.CustomResourceScheduler;
import io.stackgres.operator.common.ClusterPatchResumer;
import io.stackgres.operator.conciliation.ComparisonDelegator;
import io.stackgres.operator.conciliation.ReconciliationResult;
import io.stackgres.operator.conciliation.StackGresReconciliator;
import io.stackgres.operator.conciliation.StatusManager;
import org.slf4j.helpers.MessageFormatter;

@ApplicationScoped
public class ClusterReconciliator
    extends StackGresReconciliator<StackGresCluster> {

  private StatusManager<StackGresCluster, StackGresClusterCondition> statusManager;

  private EventEmitter<StackGresCluster> eventController;

  private CustomResourceScheduler<StackGresCluster> clusterScheduler;

  private ClusterPatchResumer patchResumer;

  @Override
  public void onPreReconciliation(StackGresCluster config) {
  }

  @Override
  public void onPostReconciliation(StackGresCluster config) {
    statusManager.refreshCondition(config);
    clusterScheduler.updateStatus(config);
  }

  @Override
  public void onConfigCreated(StackGresCluster cluster, ReconciliationResult result) {
    final String resourceChanged = patchResumer.resourceChanged(cluster, result);
    eventController.sendEvent(ClusterEventReason.CLUSTER_CREATED,
        "Cluster " + cluster.getMetadata().getNamespace() + "."
            + cluster.getMetadata().getName() + " created: " + resourceChanged, cluster);
    statusManager.updateCondition(
        ClusterStatusCondition.FALSE_FAILED.getCondition(), cluster);
  }

  @Override
  public void onConfigUpdated(StackGresCluster cluster, ReconciliationResult result) {
    final String resourceChanged = patchResumer.resourceChanged(cluster, result);
    eventController.sendEvent(ClusterEventReason.CLUSTER_UPDATED,
        "Cluster " + cluster.getMetadata().getNamespace() + "."
            + cluster.getMetadata().getName() + " updated: " + resourceChanged, cluster);
    statusManager.updateCondition(
        ClusterStatusCondition.FALSE_FAILED.getCondition(), cluster);
  }

  @Override
  public void onError(Exception ex, StackGresCluster cluster) {
    String message = MessageFormatter.arrayFormat(
        "Cluster reconciliation cycle failed",
        new String[]{
        }).getMessage();
    eventController.sendEvent(ClusterEventReason.CLUSTER_CONFIG_ERROR,
        message + ": " + ex.getMessage(), cluster);
  }

  @Inject
  public void setStatusManager(
      StatusManager<StackGresCluster, StackGresClusterCondition> statusManager) {
    this.statusManager = statusManager;
  }

  @Inject
  public void setEventController(
      @EventEmitterType(StackGresCluster.class)
          EventEmitter<StackGresCluster> eventController) {
    this.eventController = eventController;
  }

  @Inject
  public void setClusterScheduler(CustomResourceScheduler<StackGresCluster> clusterScheduler) {
    this.clusterScheduler = clusterScheduler;
  }

  @Inject
  public void setResourceComparator(ComparisonDelegator<StackGresCluster> resourceComparator) {
    this.patchResumer = new ClusterPatchResumer(resourceComparator);
  }
}
