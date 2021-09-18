/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.resource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.stackgres.common.CdiUtil;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfigList;

@ApplicationScoped
public class PostgresConfigFinder
    extends AbstractCustomResourceFinder<StackGresPostgresConfig> {

  /**
   * Create a {@code PostgresConfigFinder} instance.
   */
  @Inject
  public PostgresConfigFinder(KubernetesClient client) {
    super(client, StackGresPostgresConfig.class, StackGresPostgresConfigList.class);
  }

  public PostgresConfigFinder() {
    super(null, null, null);
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
  }

}
