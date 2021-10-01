/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.sidecars.pooling;

import static io.stackgres.operator.conciliation.VolumeMountProviderName.CONTAINER_USER_OVERRIDE;
import static io.stackgres.operator.conciliation.VolumeMountProviderName.POSTGRES_SOCKET;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.common.EnvoyUtil;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigPgBouncer;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigPgBouncerPgbouncerIni;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigPgBouncerStatus;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigSpec;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfigStatus;
import io.stackgres.operator.common.Sidecar;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.ClusterRunningContainer;
import io.stackgres.operator.conciliation.factory.ContainerContext;
import io.stackgres.operator.conciliation.factory.ImmutableVolumePair;
import io.stackgres.operator.conciliation.factory.ProviderName;
import io.stackgres.operator.conciliation.factory.RunningContainer;
import io.stackgres.operator.conciliation.factory.VolumeMountsProvider;
import io.stackgres.operator.conciliation.factory.VolumePair;
import io.stackgres.operator.conciliation.factory.cluster.StackGresClusterContainerContext;
import io.stackgres.operator.conciliation.factory.cluster.StatefulSetDynamicVolumes;
import io.stackgres.operator.conciliation.factory.cluster.sidecars.pooling.parameters.PgBouncerBlocklist;
import io.stackgres.operator.conciliation.factory.cluster.sidecars.pooling.parameters.PgBouncerDefaultValues;
import org.jetbrains.annotations.NotNull;

@Sidecar("connection-pooling")
@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V10A1, stopAt = StackGresVersion.V10)
@RunningContainer(ClusterRunningContainer.PGBOUNCER)
public class PgBouncerPooling extends AbstractPgPooling {

  private final VolumeMountsProvider<ContainerContext> containerUserOverrideMounts;
  private final VolumeMountsProvider<ContainerContext> postgresSocket;

  @Inject
  protected PgBouncerPooling(LabelFactoryForCluster<StackGresCluster> labelFactory,
                             @ProviderName(CONTAINER_USER_OVERRIDE)
                                 VolumeMountsProvider<ContainerContext> containerUserOverrideMounts,
                             @ProviderName(POSTGRES_SOCKET)
                                 VolumeMountsProvider<ContainerContext> postgresSocket) {
    super(labelFactory);
    this.containerUserOverrideMounts = containerUserOverrideMounts;
    this.postgresSocket = postgresSocket;
  }

  @Override
  protected Map<String, String> getDefaultParameters()  {
    return ImmutableMap.<String, String>builder()
        .put("listen_port", Integer.toString(EnvoyUtil.PG_POOL_PORT))
        .put("unix_socket_dir", ClusterStatefulSetPath.PG_RUN_PATH.path())
        .put("auth_file", ClusterStatefulSetPath.PGBOUNCER_AUTH_FILE_PATH.path())
        .build();
  }

  @Override
  public @NotNull Stream<VolumePair> buildVolumes(@NotNull StackGresClusterContext context) {
    return Stream.of(
        ImmutableVolumePair.builder()
        .volume(buildVolume(context))
        .source(buildSource(context))
        .build(),
        ImmutableVolumePair.builder()
        .volume(buildAuthFileVolume())
        .build(),
        ImmutableVolumePair.builder()
        .volume(buildSecretVolume(context))
        .build());
  }

  private Volume buildAuthFileVolume() {
    return new VolumeBuilder()
        .withName(StatefulSetDynamicVolumes.PGBOUNCER_AUTH_FILE.getVolumeName())
        .withEmptyDir(new EmptyDirVolumeSourceBuilder()
            .build())
        .build();
  }

  private Volume buildSecretVolume(StackGresClusterContext context) {
    return new VolumeBuilder()
        .withName(StatefulSetDynamicVolumes.PGBOUNCER_SECRETS.getVolumeName())
        .withSecret(new SecretVolumeSourceBuilder()
            .withSecretName(context.getCluster().getMetadata().getName())
            .build())
        .build();
  }

  @Override
  protected String getConfigFile(Optional<StackGresPoolingConfig> poolingConfig) {
    return ""
        + getDatabaseSection(poolingConfig)
        + getUserSection(poolingConfig)
        + getPgBouncerSection(poolingConfig);
  }

  @Override
  protected List<VolumeMount> getVolumeMounts(StackGresClusterContainerContext context) {
    return ImmutableList.<VolumeMount>builder()
        .addAll(postgresSocket.getVolumeMounts(context))
        .add(new VolumeMountBuilder()
            .withName(StatefulSetDynamicVolumes.PGBOUNCER.getVolumeName())
            .withMountPath(ClusterStatefulSetPath.PGBOUNCER_CONFIG_FILE_PATH.path())
            .withSubPath("pgbouncer.ini")
            .withReadOnly(true)
            .build())
        .add(new VolumeMountBuilder()
            .withName(StatefulSetDynamicVolumes.PGBOUNCER_AUTH_FILE.getVolumeName())
            .withMountPath(ClusterStatefulSetPath.PGBOUNCER_AUTH_PATH.path())
            .withSubPath(ClusterStatefulSetPath.PGBOUNCER_AUTH_PATH.subPath())
            .withReadOnly(true)
            .build())
        .addAll(
            containerUserOverrideMounts.getVolumeMounts(context))
        .build();
  }

  private String getPgBouncerSection(Optional<StackGresPoolingConfig> poolingConfig) {
    var newParams = poolingConfig
        .map(StackGresPoolingConfig::getSpec)
        .map(StackGresPoolingConfigSpec::getPgBouncer)
        .map(StackGresPoolingConfigPgBouncer::getPgbouncerIni)
        .map(StackGresPoolingConfigPgBouncerPgbouncerIni::getParameters)
        .orElseGet(HashMap::new);

    // Blocklist removal
    PgBouncerBlocklist.getBlocklistParameters().forEach(newParams::remove);

    Map<String, String> parameters = poolingConfig
        .map(StackGresPoolingConfig::getStatus)
        .map(StackGresPoolingConfigStatus::getPgBouncer)
        .map(StackGresPoolingConfigPgBouncerStatus::getDefaultParameters)
        .map(HashMap::new)
        .orElseGet(() -> new HashMap<>(PgBouncerDefaultValues.getDefaultValues()));

    parameters.putAll(defaultParameters);
    parameters.putAll(newParams);

    String pgBouncerConfig = parameters.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " = " + entry.getValue())
        .collect(Collectors.joining("\n"));

    return "[pgbouncer]\n" + pgBouncerConfig + "\n";
  }

  private String getUserSection(Optional<StackGresPoolingConfig> poolingConfig) {
    var users = poolingConfig
        .map(StackGresPoolingConfig::getSpec)
        .map(StackGresPoolingConfigSpec::getPgBouncer)
        .map(StackGresPoolingConfigPgBouncer::getPgbouncerIni)
        .map(StackGresPoolingConfigPgBouncerPgbouncerIni::getUsers)
        .orElseGet(HashMap::new);

    return !users.isEmpty()
        ? "[users]\n" + getSections(users) + "\n\n"
        : "";
  }

  private String getDatabaseSection(Optional<StackGresPoolingConfig> poolingConfig) {
    var databases = poolingConfig
        .map(StackGresPoolingConfig::getSpec)
        .map(StackGresPoolingConfigSpec::getPgBouncer)
        .map(StackGresPoolingConfigPgBouncer::getPgbouncerIni)
        .map(StackGresPoolingConfigPgBouncerPgbouncerIni::getDatabases)
        .orElseGet(HashMap::new);

    return !databases.isEmpty()
        ? "[databases]\n" + getSections(databases) + "\n\n"
        : "[databases]\n" + "* = port=" + EnvoyUtil.PG_PORT + "\n\n";
  }

  private String getSections(Map<String, Map<String, String>> sections) {
    return sections.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " = " + entry.getValue().entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(" ")))
        .collect(Collectors.joining("\n"));
  }
}
