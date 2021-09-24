/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1beta1.CronJob;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.operator.customresource.prometheus.ServiceMonitor;
import io.stackgres.operator.customresource.prometheus.ServiceMonitorList;
import io.stackgres.operatorframework.resource.ResourceUtil;

public interface ReconciliationOperations {

  ImmutableMap<
      Class<? extends HasMetadata>,
      Function<
          KubernetesClient,
          MixedOperation<
              ? extends HasMetadata,
              ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>>
      IN_NAMESPACE_RESOURCE_OPERATIONS =
      ImmutableMap.<Class<? extends HasMetadata>, Function<KubernetesClient,
          MixedOperation<? extends HasMetadata,
              ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>>builder()
          .put(StatefulSet.class, client -> client.apps().statefulSets())
          .put(Service.class, KubernetesClient::services)
          .put(ServiceAccount.class, KubernetesClient::serviceAccounts)
          .put(Role.class, client -> client.rbac().roles())
          .put(RoleBinding.class, client -> client.rbac().roleBindings())
          .put(Secret.class, KubernetesClient::secrets)
          .put(ConfigMap.class, KubernetesClient::configMaps)
          .put(Endpoints.class, KubernetesClient::endpoints)
          .put(CronJob.class, client -> client.batch().v1beta1().cronjobs())
          .put(Pod.class, KubernetesClient::pods)
          .put(Job.class, client -> client.batch().v1().jobs())
          .build();

  Map<
      Class<? extends HasMetadata>,
      BiFunction<
          KubernetesClient,
          Map<String, String>,
          List<HasMetadata>>> ANY_NAMESPACE_RESOURCE_OPERATIONS =
      Map.of(ServiceMonitor.class, (client, labels) -> {
        final String crdName = CustomResource.getCRDName(ServiceMonitor.class);
        var resources = ImmutableList.<HasMetadata>builder();
        if (ResourceUtil.getCustomResource(client, crdName).isPresent()) {
          resources.addAll(client.resources(ServiceMonitor.class, ServiceMonitorList.class)
              .inAnyNamespace()
              .withLabels(labels)
              .list()
              .getItems());
        }
        return resources.build();
      });

}
