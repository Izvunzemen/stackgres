/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.cluster;

import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.common.CdiUtil;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.StackGresKubernetesClient;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.conciliation.DeployedResourcesScanner;
import io.stackgres.operator.conciliation.ReconciliationOperations;

@ApplicationScoped
public class ClusterDeployedResourceScanner extends DeployedResourcesScanner<StackGresCluster>
    implements ReconciliationOperations {

  private final KubernetesClient client;
  private final LabelFactoryForCluster<StackGresCluster> labelFactory;

  @Inject
  public ClusterDeployedResourceScanner(
      KubernetesClient client,
      LabelFactoryForCluster<StackGresCluster> labelFactory) {
    this.client = client;
    this.labelFactory = labelFactory;
  }

  public ClusterDeployedResourceScanner() {
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
    this.client = null;
    this.labelFactory = null;
  }

  @Override
  protected Map<String, String> getGenericLabels(StackGresCluster config) {
    return labelFactory.genericLabels(config);
  }

  @Override
  protected StackGresKubernetesClient getClient() {
    return (StackGresKubernetesClient) client;
  }

  @Override
  protected Map<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getInNamepspaceResourceOperations() {
    return IN_NAMESPACE_RESOURCE_OPERATIONS;
  }

  @Override
  protected Map<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getAnyNamespaceResourceOperations() {
    return ANY_NAMESPACE_RESOURCE_OPERATIONS;
  }

}
