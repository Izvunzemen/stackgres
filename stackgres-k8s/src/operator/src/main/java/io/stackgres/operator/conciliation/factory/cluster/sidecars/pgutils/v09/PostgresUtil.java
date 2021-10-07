/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.sidecars.pgutils.v09;

import static io.stackgres.operator.conciliation.VolumeMountProviderName.CONTAINER_LOCAL_OVERRIDE;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.stackgres.common.StackgresClusterContainers;
import io.stackgres.operator.common.Sidecar;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.factory.ClusterRunningContainer;
import io.stackgres.operator.conciliation.factory.ContainerContext;
import io.stackgres.operator.conciliation.factory.ProviderName;
import io.stackgres.operator.conciliation.factory.RunningContainer;
import io.stackgres.operator.conciliation.factory.VolumeMountsProvider;
import io.stackgres.operator.conciliation.factory.cluster.StackGresClusterContainerContext;
import io.stackgres.operator.conciliation.factory.cluster.sidecars.pgutils.AbstractPostgresUtil;

@Sidecar(StackgresClusterContainers.POSTGRES_UTIL)
@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V09, stopAt = StackGresVersion.V09_LAST)
@RunningContainer(ClusterRunningContainer.POSTGRES_UTIL_V09)
public class PostgresUtil extends AbstractPostgresUtil {

  private static final String IMAGE_NAME = "docker.io/ongres/postgres-util:v%s-build-6.0";
  private VolumeMountsProvider<ContainerContext> containerLocalOverrideMounts;

  @Override
  public Container getContainer(StackGresClusterContainerContext context) {
    return new ContainerBuilder()
        .withName(StackgresClusterContainers.POSTGRES_UTIL)
        .withImage(String.format(IMAGE_NAME,
            context.getClusterContext().getSource().getSpec().getPostgres().getVersion()))
        .withImagePullPolicy("IfNotPresent")
        .withStdin(Boolean.TRUE)
        .withTty(Boolean.TRUE)
        .withCommand("/bin/sh")
        .withArgs("-c", "while true; do sleep 10; done")
        .addAllToVolumeMounts(postgresSocket.getVolumeMounts(context))
        .addAllToVolumeMounts(containerLocalOverrideMounts.getVolumeMounts(context))
        .build();
  }

  @Inject
  public void setContainerUserOverrideMounts(
      @ProviderName(CONTAINER_LOCAL_OVERRIDE)
          VolumeMountsProvider<ContainerContext> containerUserOverrideMounts) {
    this.containerLocalOverrideMounts = containerUserOverrideMounts;
  }

}
