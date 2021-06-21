/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.distributedlogs.v09;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpec;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.factory.distributedlogs.AbstractDistributedLogsAnnotationDecorator;
import org.jetbrains.annotations.NotNull;

@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V09, stopAt = StackGresVersion.V09_LAST)
public class DistributedLogsAnnotationDecorator extends AbstractDistributedLogsAnnotationDecorator {

  @Override
  protected void decorateSts(@NotNull StackGresDistributedLogs cluster,
                             @NotNull HasMetadata resource) {
    StatefulSet statefulSet = (StatefulSet) resource;
    Map<String, String> podTemplateAnnotations = Optional.ofNullable(statefulSet.getSpec())
        .map(StatefulSetSpec::getTemplate)
        .map(PodTemplateSpec::getMetadata)
        .map(ObjectMeta::getAnnotations)
        .orElse(new HashMap<>());

    podTemplateAnnotations.putAll(getPodAnnotations(cluster));
    Optional.ofNullable(statefulSet.getSpec())
        .map(StatefulSetSpec::getTemplate)
        .ifPresent(template -> {
          final ObjectMeta metadata = Optional
              .ofNullable(template.getMetadata())
              .orElse(new ObjectMeta());
          metadata.setAnnotations(podTemplateAnnotations);
          template.setMetadata(metadata);
        });

    Optional.ofNullable(statefulSet.getSpec())
        .map(StatefulSetSpec::getVolumeClaimTemplates)
        .ifPresent(cvt -> decorate(cluster, cvt));
  }
}
