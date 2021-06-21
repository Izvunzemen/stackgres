/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.cluster.factory;

import static io.stackgres.common.StringUtil.generateRandom;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobTemplateSpecBuilder;
import io.stackgres.common.PatroniUtil;

public class KubernetessMockResourceGenerationUtil {

  public static List<HasMetadata> buildResources(String cluster, String namespace) {
    return Stream.of(
        new SecretBuilder()
            .withData(ImmutableMap.of(generateRandom(), generateRandom()))
            .withNewMetadata()
            .withName(cluster)
            .withNamespace(namespace)
            .endMetadata().build(),
        new ConfigMapBuilder()
            .withData(ImmutableMap.of(generateRandom(), generateRandom()))
            .withNewMetadata()
            .withName(cluster)
            .withNamespace(namespace)
            .endMetadata()
            .build(),
        new ConfigMapBuilder()
            .withData(ImmutableMap.of(generateRandom(), generateRandom()))
            .withNewMetadata().withName(cluster + "-templates")
            .withNamespace(namespace)
            .endMetadata()
            .build(),
        new StatefulSetBuilder()
            .withNewMetadata()
            .withNewName(cluster)
            .withNamespace(namespace)
            .endMetadata()
            .withNewSpec()
            .withReplicas(2)
            .withTemplate(
                new PodTemplateSpecBuilder()
                    .withNewSpec()
                    .addNewContainer().withImage(generateRandom())
                    .endContainer()
                    .endSpec()
                    .build())
            .withVolumeClaimTemplates(
                new PersistentVolumeClaimBuilder()
                    .withNewMetadata()
                    .withName(generateRandom())
                    .endMetadata()
                    .withNewSpec()
                    .withAccessModes("ReadWriteOnce")
                    .endSpec()
                    .build())
            .endSpec().build(),
        new ServiceBuilder()
            .withNewMetadata()
            .withName(cluster + "-" + PatroniUtil.READ_WRITE_SERVICE)
            .withLabels(ImmutableMap.of(generateRandom(), generateRandom()))
            .withNamespace(namespace)
            .endMetadata()
            .build(),
        new ServiceBuilder()
            .withNewMetadata()
            .withLabels(ImmutableMap.of(generateRandom(), generateRandom()))
            .withName(cluster + "-" + PatroniUtil.READ_ONLY_SERVICE)
            .withNamespace(namespace)
            .endMetadata()
            .build(),
        new PodBuilder()
            .withNewMetadata().withName(cluster + "-0").withNamespace(namespace)
            .endMetadata()
            .withSpec(new PodSpecBuilder()
                .addNewContainer()
                .withImage(generateRandom())
                .endContainer()
                .build())
            .build(),
        new CronJobBuilder()
            .withNewMetadata()
            .withName(cluster + "-backup")
            .withNamespace(namespace)
            .endMetadata()
            .withNewSpec()
            .withJobTemplate(new JobTemplateSpecBuilder()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .addNewContainer()
                .withImage(generateRandom())
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build())
            .endSpec()
            .build(),
        new JobBuilder()
            .withNewMetadata()
            .withName(cluster)
            .withNamespace(namespace)
            .endMetadata()
            .withNewSpec()
            .withNewTemplateLike(new PodTemplateSpecBuilder()
                .withNewSpec().addNewContainer()
                .withImage(generateRandom())
                .endContainer().endSpec()
                .build())
            .endTemplate()
            .endSpec()
            .build()
    ).collect(Collectors.toList());
  }
}
