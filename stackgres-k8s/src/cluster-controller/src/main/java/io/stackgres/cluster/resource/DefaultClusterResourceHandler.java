/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.cluster.resource;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.stackgres.cluster.common.StackGresClusterContext;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.crd.sgcluster.StackGresCluster;

@ApplicationScoped
public class DefaultClusterResourceHandler
    extends AbstractClusterResourceHandler {

  private final LabelFactory<StackGresCluster> labelFactory;

  @Inject
  public DefaultClusterResourceHandler(
      LabelFactory<StackGresCluster> labelFactory) {
    this.labelFactory = labelFactory;
  }

  @Override
  public Stream<HasMetadata> getResources(KubernetesClient client,
      StackGresClusterContext context) {
    return STACKGRES_CLUSTER_RESOURCE_OPERATIONS.values()
        .stream()
        .flatMap(resourceOperationGetter -> {
          return resourceOperationGetter.apply(client)
              .inNamespace(context.getCluster().getMetadata().getNamespace())
              .withLabels(labelFactory.clusterLabels(context.getCluster()))
              .list()
              .getItems()
              .stream();
        });
  }

}
