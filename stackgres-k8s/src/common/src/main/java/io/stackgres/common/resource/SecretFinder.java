/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.resource;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class SecretFinder implements
    ResourceFinder<Secret>,
    ResourceScanner<Secret> {

  final KubernetesClient client;

  @Inject
  public SecretFinder(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public Optional<Secret> findByName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Secret> findByNameAndNamespace(String name, String namespace) {
    return Optional.ofNullable(client.secrets().inNamespace(namespace).withName(name).get());
  }

  @Override
  public List<Secret> findResources() {
    return client.secrets().inAnyNamespace().list().getItems();
  }

  @Override
  public List<Secret> findResourcesInNamespace(String namespace) {
    return client.secrets().inNamespace(namespace).list().getItems();
  }

}
