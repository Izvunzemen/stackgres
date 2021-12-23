/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.common.StackGresKubernetesClient;
import io.stackgres.common.resource.ResourceWriter;

public abstract class DeployedResourcesScanner<T extends CustomResource<?, ?>> {

  public List<HasMetadata> getDeployedResources(T config) {
    final String kind = HasMetadata.getKind(config.getClass());
    final Map<String, String> genericClusterLabels = getGenericLabels(config);

    StackGresKubernetesClient stackGresClient = getClient();

    Stream<HasMetadata> inNamespace = getInNamepspaceResourceOperations()
        .keySet()
        .stream()
        .flatMap(clazz -> stackGresClient.findManagedIntents(
            clazz,
            ResourceWriter.STACKGRES_FIELD_MANAGER,
            genericClusterLabels,
            config.getMetadata().getNamespace())
            .stream());

    Stream<HasMetadata> anyNamespace = getAnyNamespaceResourceOperations()
        .keySet()
        .stream()
        .flatMap(clazz -> stackGresClient.findManagedIntents(
            clazz,
            ResourceWriter.STACKGRES_FIELD_MANAGER,
            genericClusterLabels,
            config.getMetadata().getNamespace())
            .stream());

    List<HasMetadata> deployedResources = Stream.concat(inNamespace, anyNamespace)
        .filter(resource -> resource.getMetadata().getOwnerReferences()
            .stream().anyMatch(ownerReference -> ownerReference.getKind()
                .equals(kind)
                && ownerReference.getName().equals(config.getMetadata().getName())
                && ownerReference.getUid().equals(config.getMetadata().getUid())))
        .collect(Collectors.toUnmodifiableList());

    return deployedResources;
  }

  protected abstract Map<String, String> getGenericLabels(T config);

  protected abstract StackGresKubernetesClient getClient();

  protected abstract ImmutableMap<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getAnyNamespaceResourceOperations();

  protected abstract ImmutableMap<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getInNamepspaceResourceOperations();

}
