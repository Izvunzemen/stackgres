/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.distributedlogs.controller;

import static io.stackgres.operator.conciliation.VolumeMountProviderName.CONTAINER_USER_OVERRIDE;
import static io.stackgres.operator.conciliation.VolumeMountProviderName.POSTGRES_DATA;
import static io.stackgres.operator.conciliation.VolumeMountProviderName.POSTGRES_SOCKET;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.stackgres.common.DistributedLogsControllerProperty;
import io.stackgres.common.OperatorProperty;
import io.stackgres.common.StackGresController;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.factory.ContainerContext;
import io.stackgres.operator.conciliation.factory.ContainerFactory;
import io.stackgres.operator.conciliation.factory.InitContainer;
import io.stackgres.operator.conciliation.factory.ProviderName;
import io.stackgres.operator.conciliation.factory.VolumeMountsProvider;
import io.stackgres.operator.conciliation.factory.distributedlogs.DistributedLogsContainerContext;
import io.stackgres.operator.conciliation.factory.distributedlogs.FluentdStaticVolume;
import io.stackgres.operator.conciliation.factory.distributedlogs.StatefulSetDynamicVolumes;

@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V10A1, stopAt = StackGresVersion.V10)
@InitContainer(order = 5)
public class InitReconciliationCycle implements ContainerFactory<DistributedLogsContainerContext> {

  private final VolumeMountsProvider<ContainerContext> containerUserOverrideMounts;

  private final VolumeMountsProvider<ContainerContext> postgresSocket;

  private final VolumeMountsProvider<ContainerContext> postgresDataMounts;

  @Inject
  public InitReconciliationCycle(
      @ProviderName(CONTAINER_USER_OVERRIDE)
          VolumeMountsProvider<ContainerContext> containerUserOverrideMounts,
      @ProviderName(POSTGRES_SOCKET)
          VolumeMountsProvider<ContainerContext> postgresSocket,
      @ProviderName(POSTGRES_DATA)
          VolumeMountsProvider<ContainerContext> postgresDataMounts) {
    this.containerUserOverrideMounts = containerUserOverrideMounts;
    this.postgresSocket = postgresSocket;
    this.postgresDataMounts = postgresDataMounts;
  }

  @Override
  public Container getContainer(DistributedLogsContainerContext context) {
    final ObjectMeta metadata = context
        .getDistributedLogsContext()
        .getSource()
        .getMetadata();
    return new ContainerBuilder()
        .withName("distributedlogs-reconciliation-cycle")
        .withImage(StackGresController.DISTRIBUTEDLOGS_CONTROLLER.getImageName())
        .withEnv(
            new EnvVarBuilder()
                .withName("COMMAND")
                .withValue("run-reconciliation-cycle")
                .build(),
            new EnvVarBuilder()
                .withName(DistributedLogsControllerProperty.DISTRIBUTEDLOGS_NAME
                    .getEnvironmentVariableName())
                .withValue(metadata.getName())
                .build(),
            new EnvVarBuilder()
                .withName(DistributedLogsControllerProperty.DISTRIBUTEDLOGS_NAMESPACE
                    .getEnvironmentVariableName())
                .withValue(metadata.getNamespace())
                .build(),
            new EnvVarBuilder()
                .withName(DistributedLogsControllerProperty.DISTRIBUTEDLOGS_CONTROLLER_POD_NAME
                    .getEnvironmentVariableName())
                .withValueFrom(new EnvVarSourceBuilder()
                    .withFieldRef(new ObjectFieldSelector("v1", "metadata.name"))
                    .build())
                .build(),
            new EnvVarBuilder()
                .withName(DistributedLogsControllerProperty
                    .DISTRIBUTEDLOGS_CONTROLLER_EXTENSIONS_REPOSITORY_URLS
                    .getEnvironmentVariableName())
                .withValue(OperatorProperty.EXTENSIONS_REPOSITORY_URLS
                    .getString())
                .build(),
            new EnvVarBuilder()
                .withName(DistributedLogsControllerProperty
                    .DISTRIBUTEDLOGS_CONTROLLER_SKIP_OVERWRITE_SHARED_LIBRARIES
                    .getEnvironmentVariableName())
                .withValue(Boolean.FALSE.toString())
                .build(),
            new EnvVarBuilder()
                .withName("DISTRIBUTEDLOGS_CONTROLLER_LOG_LEVEL")
                .withValue(System.getenv("OPERATOR_LOG_LEVEL"))
                .build(),
            new EnvVarBuilder()
                .withName("DISTRIBUTEDLOGS_CONTROLLER_SHOW_STACK_TRACES")
                .withValue(System.getenv("OPERATOR_SHOW_STACK_TRACES"))
                .build(),
            new EnvVarBuilder()
                .withName("APP_OPTS")
                .withValue(System.getenv("APP_OPTS"))
                .build(),
            new EnvVarBuilder()
                .withName("DEBUG_DISTRIBUTEDLOGS_CONTROLLER")
                .withValue(System.getenv("DEBUG_OPERATOR"))
                .build(),
            new EnvVarBuilder()
                .withName("DEBUG_DISTRIBUTEDLOGS_CONTROLLER_SUSPEND")
                .withValue(System.getenv("DEBUG_OPERATOR_SUSPEND"))
                .build()
        )
        .addAllToVolumeMounts(postgresSocket.getVolumeMounts(context))
        .addAllToVolumeMounts(postgresDataMounts.getVolumeMounts(context))
        .addToVolumeMounts(
            new VolumeMountBuilder()
                .withName(FluentdStaticVolume.FLUENTD.getVolumeName())
                .withMountPath("/fluentd")
                .withReadOnly(Boolean.FALSE)
                .build(),
            new VolumeMountBuilder()
                .withName(StatefulSetDynamicVolumes.FLUENTD_CONFIG.getVolumeName())
                .withMountPath("/etc/fluentd")
                .withReadOnly(Boolean.TRUE)
                .build()
        )
        .addAllToVolumeMounts(containerUserOverrideMounts.getVolumeMounts(context))
        .build();
  }

  @Override
  public Map<String, String> getComponentVersions(DistributedLogsContainerContext context) {
    return Map.of();
  }
}
