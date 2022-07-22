/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.cluster;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;
import io.stackgres.common.crd.sgprofile.StackGresProfile;
import io.stackgres.operator.common.Prometheus;
import io.stackgres.operator.conciliation.AbstractRequiredResourceDecoratorTest;
import io.stackgres.operator.conciliation.RequiredResourceDecorator;
import io.stackgres.operator.fixture.SecretFixture;
import io.stackgres.operator.fixture.StackGresClusterFixture;
import io.stackgres.operator.fixture.StackGresPoolingConfigFixture;
import io.stackgres.operator.fixture.StackGresPostgresConfigFixture;
import io.stackgres.operator.fixture.StackGresProfileFixture;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@WithKubernetesTestServer
class ClusterRequiredResourceDecoratorTest
    extends AbstractRequiredResourceDecoratorTest<StackGresClusterContext> {

  @Inject
  ClusterRequiredResourceDecorator resourceDecorator;

  private StackGresCluster resource;
  private StackGresPostgresConfig pgConfig;
  private StackGresProfile profile;
  private Optional<StackGresBackupConfig> backupConfig;
  private Optional<StackGresPoolingConfig> pooling;
  private Optional<Secret> secret;

  @BeforeEach
  public void setup() {
    this.resource = new StackGresClusterFixture().build("default");
    this.pgConfig = new StackGresPostgresConfigFixture().build("default_postgres");
    this.profile = new StackGresProfileFixture().build("size-s");
    this.backupConfig = ofNullable(null);
    this.pooling = ofNullable(new StackGresPoolingConfigFixture().build("default"));
    this.secret = ofNullable(new SecretFixture().build("minio"));
  }

  @Override
  protected RequiredResourceDecorator<StackGresClusterContext> getResourceDecorator() {
    return this.resourceDecorator;
  }

  @Override
  protected StackGresClusterContext getResourceContext() throws IOException {
    return ImmutableStackGresClusterContext.builder()
        .source(resource)
        .postgresConfig(pgConfig)
        .profile(profile)
        .backupConfig(backupConfig)
        .poolingConfig(pooling)
        .prometheus(new Prometheus(false, null))
        .databaseCredentials(secret)
        .build();
  }

  @Override
  protected String usingCrdFilename() {
    return "SGCluster.yaml";
  }

  @Override
  protected HasMetadata getResource() {
    return resource;
  }

}
