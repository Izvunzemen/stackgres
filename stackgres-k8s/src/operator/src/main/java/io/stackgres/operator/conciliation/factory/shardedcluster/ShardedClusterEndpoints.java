/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.shardedcluster;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.EndpointPortBuilder;
import io.fabric8.kubernetes.api.model.EndpointsBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.stackgres.common.labels.LabelFactoryForShardedCluster;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.shardedcluster.StackGresShardedClusterContext;

@Singleton
@OperatorVersionBinder
public class ShardedClusterEndpoints implements ResourceGenerator<StackGresShardedClusterContext> {

  private final LabelFactoryForShardedCluster labelFactory;

  @Inject
  public ShardedClusterEndpoints(LabelFactoryForShardedCluster labelFactory) {
    this.labelFactory = labelFactory;
  }

  @Override
  public Stream<HasMetadata> generateResource(StackGresShardedClusterContext context) {
    return context.getCoordinatorPrimaryService()
        .stream()
        .filter(service -> service.getSpec() != null)
        .filter(service -> service.getSpec().getClusterIP() != null)
        .filter(service -> service.getSpec().getPorts() != null)
        .map(service -> new EndpointsBuilder()
            .withNewMetadata()
            .withNamespace(context.getSource().getMetadata().getNamespace())
            .withName(ShardedClusterServices.readWriteName(context))
            .addToLabels(labelFactory.genericLabels(context.getSource()))
            .endMetadata()
            .addNewSubset()
            .addNewAddress()
            .withIp(service.getSpec().getClusterIP())
            .endAddress()
            .addAllToPorts(Optional.of(service.getSpec())
                .map(ServiceSpec::getPorts)
                .stream()
                .flatMap(List::stream)
                .map(servicePort -> new EndpointPortBuilder()
                    .withAppProtocol(servicePort.getAppProtocol())
                    .withName(servicePort.getName())
                    .withPort(servicePort.getPort())
                    .withProtocol(servicePort.getProtocol())
                    .build())
                .toList())
            .endSubset()
            .build());
  }

}
