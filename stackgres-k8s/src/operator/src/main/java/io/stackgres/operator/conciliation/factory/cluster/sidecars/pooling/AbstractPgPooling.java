/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.sidecars.pooling;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.StackGresComponent;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterPod;
import io.stackgres.common.crd.sgcluster.StackGresClusterSpec;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.ContainerFactory;
import io.stackgres.operator.conciliation.factory.ImmutableVolumePair;
import io.stackgres.operator.conciliation.factory.VolumeFactory;
import io.stackgres.operator.conciliation.factory.VolumePair;
import io.stackgres.operator.conciliation.factory.cluster.StackGresClusterContainerContext;
import io.stackgres.operator.conciliation.factory.cluster.StatefulSetDynamicVolumes;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPgPooling
    implements ContainerFactory<StackGresClusterContainerContext>,
    VolumeFactory<StackGresClusterContext> {

  private static final String NAME = "pgbouncer";

  protected final Map<String, String> defaultParameters = getDefaultParameters();

  private final LabelFactoryForCluster<StackGresCluster> labelFactory;

  @Inject
  protected AbstractPgPooling(
      LabelFactoryForCluster<StackGresCluster> labelFactory) {
    this.labelFactory = labelFactory;
  }

  public static String configName(StackGresClusterContext clusterContext) {
    String name = clusterContext.getSource().getMetadata().getName();
    return StatefulSetDynamicVolumes.PGBOUNCER.getResourceName(name);
  }

  protected abstract Map<String, String> getDefaultParameters();

  @Override
  public boolean isActivated(StackGresClusterContainerContext context) {
    return Optional.ofNullable(context)
        .map(StackGresClusterContainerContext::getClusterContext)
        .map(StackGresClusterContext::getSource)
        .map(StackGresCluster::getSpec)
        .map(StackGresClusterSpec::getPod)
        .map(StackGresClusterPod::getDisableConnectionPooling)
        .map(disable -> !disable)
        .orElse(Boolean.TRUE);
  }

  @Override
  public @NotNull Stream<VolumePair> buildVolumes(@NotNull StackGresClusterContext context) {
    return Stream.of(
        ImmutableVolumePair.builder()
        .volume(buildVolume(context))
        .source(buildSource(context))
        .build());
  }

  protected Volume buildVolume(StackGresClusterContext context) {
    return new VolumeBuilder()
        .withName(StatefulSetDynamicVolumes.PGBOUNCER.getVolumeName())
        .withConfigMap(new ConfigMapVolumeSourceBuilder()
            .withName(configName(context))
            .build())
        .build();
  }

  protected HasMetadata buildSource(@NotNull StackGresClusterContext context) {
    final StackGresCluster sgCluster = context.getSource();

    Map<String, String> data = getConfigMapData(context);

    String namespace = sgCluster.getMetadata().getNamespace();
    String configMapName = configName(context);

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withNamespace(namespace)
        .withName(configMapName)
        .withLabels(labelFactory.genericLabels(sgCluster))
        .endMetadata()
        .withData(data)
        .build();
  }

  protected Map<String, String> getConfigMapData(StackGresClusterContext context) {
    String configFile = getConfigFile(context.getPoolingConfig());
    Map<String, String> data = ImmutableMap.of("pgbouncer.ini", configFile);
    return data;
  }

  @Override
  public Container getContainer(StackGresClusterContainerContext context) {
    return new ContainerBuilder()
        .withName(NAME)
        .withImage(getImageName())
        .withImagePullPolicy("IfNotPresent")
        .withVolumeMounts(getVolumeMounts(context))
        .build();
  }

  @Override
  public Map<String, String> getComponentVersions(StackGresClusterContainerContext context) {
    return ImmutableMap.of(
        StackGresContext.PGBOUNCER_VERSION_KEY,
        StackGresComponent.PGBOUNCER.findLatestVersion());
  }

  protected String getImageName() {
    return StackGresComponent.PGBOUNCER.findLatestImageName();
  }

  protected abstract List<VolumeMount> getVolumeMounts(StackGresClusterContainerContext context);

  protected abstract String getConfigFile(Optional<StackGresPoolingConfig> pgbouncerConfig);

}
