/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.sidecars.pgexporter;

import static io.stackgres.operator.conciliation.VolumeMountProviderName.CONTAINER_USER_OVERRIDE;
import static io.stackgres.operator.conciliation.VolumeMountProviderName.POSTGRES_SOCKET;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.common.EnvoyUtil;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.StackGresComponent;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.StackgresClusterContainers;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.common.Sidecar;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.ContainerContext;
import io.stackgres.operator.conciliation.factory.ContainerFactory;
import io.stackgres.operator.conciliation.factory.ImmutableVolumePair;
import io.stackgres.operator.conciliation.factory.ProviderName;
import io.stackgres.operator.conciliation.factory.RunningContainer;
import io.stackgres.operator.conciliation.factory.VolumeFactory;
import io.stackgres.operator.conciliation.factory.VolumeMountsProvider;
import io.stackgres.operator.conciliation.factory.VolumePair;
import io.stackgres.operator.conciliation.factory.cluster.StackGresClusterContainerContext;
import io.stackgres.operator.conciliation.factory.cluster.StatefulSetDynamicVolumes;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Sidecar(PostgresExporter.NAME)
@OperatorVersionBinder(startAt = StackGresVersion.V10A1, stopAt = StackGresVersion.V10)
@RunningContainer(order = 2)
public class PostgresExporter implements ContainerFactory<StackGresClusterContainerContext>,
    VolumeFactory<StackGresClusterContext> {

  public static final String NAME = StackgresClusterContainers.POSTGRES_EXPORTER;

  private static final Logger POSTGRES_EXPORTER_LOGGER = LoggerFactory.getLogger(
      "io.stackgres.prometheus-postgres-exporter");

  private LabelFactoryForCluster<StackGresCluster> labelFactory;

  private VolumeMountsProvider<ContainerContext> containerUserOverrideMounts;

  private VolumeMountsProvider<ContainerContext> postgresSocket;

  public static String configName(StackGresClusterContext clusterContext) {
    final String name = clusterContext.getSource().getMetadata().getName();
    return StatefulSetDynamicVolumes.EXPORTER_QUERIES.getResourceName(name);
  }

  @Override
  public Container getContainer(StackGresClusterContainerContext context) {
    StackGresCluster cluster = context.getClusterContext().getSource();
    ContainerBuilder container = new ContainerBuilder();
    container.withName(NAME)
        .withImage(StackGresComponent.PROMETHEUS_POSTGRES_EXPORTER.findLatestImageName())
        .withImagePullPolicy("IfNotPresent")
        .withCommand("/bin/sh", "-exc")
        .withArgs(""
            + "run_postgres_exporter() {\n"
            + "  set -x\n"
            + "  exec /usr/local/bin/postgres_exporter \\\n"
            + "    --log.level="
            + (POSTGRES_EXPORTER_LOGGER.isTraceEnabled() ? "debug" : "info") + "\n"
            + "}\n"
            + "\n"
            + "set +x\n"
            + "while true\n"
            + "do\n"
            + "  if ( [ -z \"$PID\" ] || [ ! -d \"/proc/$PID\" ] ) \\\n"
            + "    && [ -S '" + ClusterStatefulSetPath.PG_RUN_PATH.path()
            + "/.s.PGSQL." + EnvoyUtil.PG_PORT + "' ]\n"
            + "  then\n"
            + "    if [ -n \"$PID\" ]\n"
            + "    then\n"
            + "      kill \"$PID\"\n"
            + "      wait \"$PID\" || true\n"
            + "    fi\n"
            + "    run_postgres_exporter &\n"
            + "    PID=\"$!\"\n"
            + "  fi\n"
            + "  sleep 5\n"
            + "done\n")
        .withEnv(
            new EnvVarBuilder()
                .withName("PGAPPNAME")
                .withValue(NAME)
                .build(),
            new EnvVarBuilder()
                .withName("DATA_SOURCE_NAME")
                .withValue("postgresql://postgres@:" + EnvoyUtil.PG_PORT + "/postgres"
                    + "?host=" + ClusterStatefulSetPath.PG_RUN_PATH.path()
                    + "&sslmode=disable")
                .build(),
            new EnvVarBuilder()
                .withName("PG_EXPORTER_EXTEND_QUERY_PATH")
                .withValue("/var/opt/postgres-exporter/queries.yaml")
                .build(),
            new EnvVarBuilder()
                .withName("PG_EXPORTER_CONSTANT_LABELS")
                .withValue("cluster_name=" + cluster.getMetadata().getName()
                    + ", namespace=" + cluster.getMetadata().getNamespace())
                .build())
        .withPorts(new ContainerPortBuilder()
            .withProtocol("TCP")
            .withContainerPort(9187)
            .build())
        .addAllToVolumeMounts(postgresSocket.getVolumeMounts(context))
        .addToVolumeMounts(
            new VolumeMountBuilder()
                .withName(StatefulSetDynamicVolumes.EXPORTER_QUERIES.getVolumeName())
                .withMountPath("/var/opt/postgres-exporter/queries.yaml")
                .withSubPath("queries.yaml")
                .withReadOnly(true)
                .build()
        )
        .addAllToVolumeMounts(containerUserOverrideMounts.getVolumeMounts(context));

    return container.build();
  }

  @Override
  public Map<String, String> getComponentVersions(StackGresClusterContainerContext context) {
    return ImmutableMap.of(
        StackGresContext.PROMETHEUS_POSTGRES_EXPORTER_VERSION_KEY,
        StackGresComponent.PROMETHEUS_POSTGRES_EXPORTER.findLatestVersion());
  }

  @Override
  public @NotNull Stream<VolumePair> buildVolumes(StackGresClusterContext context) {
    return Stream.of(
        ImmutableVolumePair.builder()
            .volume(buildVolume(context))
            .source(buildSource(context))
            .build()
    );
  }

  public @NotNull Volume buildVolume(StackGresClusterContext context) {
    return new VolumeBuilder()
        .withName(StatefulSetDynamicVolumes.EXPORTER_QUERIES.getVolumeName())
        .withConfigMap(new ConfigMapVolumeSourceBuilder()
            .withName(configName(context))
            .build())
        .build();
  }

  public @NotNull HasMetadata buildSource(StackGresClusterContext context) {

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(configName(context))
        .withNamespace(context.getSource().getMetadata().getNamespace())
        .withLabels(labelFactory.clusterCrossNamespaceLabels(context.getSource()))
        .endMetadata()
        .withData(ImmutableMap.of("queries.yaml",
            Unchecked.supplier(() -> Resources
                .asCharSource(Objects.requireNonNull(PostgresExporter.class.getResource(
                    "/prometheus-postgres-exporter/queries.yaml")),
                    StandardCharsets.UTF_8)
                .read()).get()))
        .build();

  }

  @Inject
  public void setLabelFactory(LabelFactoryForCluster<StackGresCluster> labelFactory) {
    this.labelFactory = labelFactory;
  }

  @Inject
  public void setContainerUserOverrideMounts(
      @ProviderName(CONTAINER_USER_OVERRIDE)
          VolumeMountsProvider<ContainerContext> containerUserOverrideMounts) {
    this.containerUserOverrideMounts = containerUserOverrideMounts;
  }

  @Inject
  public void setPostgresSocket(
      @ProviderName(POSTGRES_SOCKET)
          VolumeMountsProvider<ContainerContext> postgresSocket) {
    this.postgresSocket = postgresSocket;
  }
}
