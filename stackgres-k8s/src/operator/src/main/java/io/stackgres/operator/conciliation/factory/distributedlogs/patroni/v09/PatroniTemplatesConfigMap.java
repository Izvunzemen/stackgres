/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.distributedlogs.patroni.v09;

import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.distributedlogs.DistributedLogsContext;
import io.stackgres.operator.conciliation.factory.ImmutableVolumePair;
import io.stackgres.operator.conciliation.factory.VolumePair;
import io.stackgres.operator.conciliation.factory.distributedlogs.StatefulSetDynamicVolumes;
import io.stackgres.operator.conciliation.factory.v09.AbstractPatroniTemplatesConfigMap;
import org.jetbrains.annotations.NotNull;

@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V09, stopAt = StackGresVersion.V09_LAST)
public class PatroniTemplatesConfigMap
    extends AbstractPatroniTemplatesConfigMap<DistributedLogsContext> {

  private LabelFactory<StackGresDistributedLogs> labelFactory;

  private static String name(DistributedLogsContext context) {
    final String clusterName = context.getSource().getMetadata().getName();
    return StatefulSetDynamicVolumes.SCRIPT_TEMPLATES.getResourceName(clusterName);
  }

  @Override
  public @NotNull Stream<VolumePair> buildVolumes(DistributedLogsContext context) {
    return Stream.of(
        ImmutableVolumePair.builder()
            .volume(buildVolume(context))
            .source(buildSource(context))
            .build()
    );
  }

  public @NotNull Volume buildVolume(DistributedLogsContext context) {
    return new VolumeBuilder()
        .withName(StatefulSetDynamicVolumes.SCRIPT_TEMPLATES.getVolumeName())
        .withConfigMap(new ConfigMapVolumeSourceBuilder()
            .withName(name(context))
            .withDefaultMode(444)
            .build())
        .build();
  }

  public @NotNull HasMetadata buildSource(DistributedLogsContext context) {

    Map<String, String> data = getPatroniTemplates();

    final StackGresDistributedLogs cluster = context.getSource();

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withNamespace(cluster.getMetadata().getNamespace())
        .withName(name(cluster))
        .withLabels(labelFactory.clusterLabels(cluster))
        .withOwnerReferences(context.getOwnerReferences())
        .endMetadata()
        .withData(data)
        .build();
  }

  @Inject
  public void setLabelFactory(LabelFactory<StackGresDistributedLogs> labelFactory) {
    this.labelFactory = labelFactory;
  }
}
