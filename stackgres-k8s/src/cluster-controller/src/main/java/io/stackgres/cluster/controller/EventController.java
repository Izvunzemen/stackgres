/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.cluster.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.stackgres.common.CdiUtil;
import io.stackgres.common.ClusterControllerProperty;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.resource.CustomResourceFinder;
import io.stackgres.operatorframework.resource.EventEmitter;
import io.stackgres.operatorframework.resource.EventReason;

@ApplicationScoped
public class EventController extends EventEmitter {

  private final CustomResourceFinder<StackGresCluster> clusterFinder;

  @Inject
  public EventController(CustomResourceFinder<StackGresCluster> clusterFinder) {
    this.clusterFinder = clusterFinder;
  }

  public EventController() {
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
    this.clusterFinder = null;
  }

  /**
   * Send an event.
   */
  public void sendEvent(EventReason reason, String message, KubernetesClient client) {
    StackGresCluster cluster = clusterFinder
        .findByNameAndNamespace(
            ClusterControllerProperty.CLUSTER_NAME.getString(),
            ClusterControllerProperty.CLUSTER_NAMESPACE.getString())
        .orElse(null);
    sendEvent(reason, message, cluster, client);
  }

}
