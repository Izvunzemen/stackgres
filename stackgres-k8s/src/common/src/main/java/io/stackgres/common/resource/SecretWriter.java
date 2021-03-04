/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.resource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Namespaceable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.common.CdiUtil;
import io.stackgres.common.KubernetesClientFactory;

@ApplicationScoped
public class SecretWriter extends AbstractResourceWriter<Secret, SecretList> {

  @Inject
  public SecretWriter(KubernetesClientFactory clientFactory) {
    super(clientFactory);
  }

  public SecretWriter() {
    super(null);
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
  }

  @Override
  protected Namespaceable<NonNamespaceOperation<Secret, SecretList, Resource<Secret>>>
      getResourceEndpoints(KubernetesClient client) {
    return client.secrets();
  }

}
