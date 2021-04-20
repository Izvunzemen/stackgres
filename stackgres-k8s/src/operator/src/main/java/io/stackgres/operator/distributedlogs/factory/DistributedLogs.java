/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.distributedlogs.factory;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.operator.cluster.factory.Cluster;
import io.stackgres.operator.common.StackGresDistributedLogsContext;
import io.stackgres.operatorframework.resource.ResourceGenerator;
import io.stackgres.operatorframework.resource.factory.SubResourceStreamFactory;

@ApplicationScoped
public class DistributedLogs
    implements SubResourceStreamFactory<HasMetadata, StackGresDistributedLogsContext> {

  private final Cluster cluster;

  @Inject
  public DistributedLogs(Cluster cluster) {
    super();
    this.cluster = cluster;
  }

  @Override
  public Stream<HasMetadata> streamResources(StackGresDistributedLogsContext context) {
    return ResourceGenerator
        .with(context)
        .of(HasMetadata.class)
        .append(cluster)
        .stream();
  }

}
