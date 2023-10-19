/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.shardeddbops;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.StackGresGroupKind;
import io.stackgres.common.StackGresProperty;
import io.stackgres.common.StringUtil;
import io.stackgres.common.crd.sgcluster.StackGresClusterNonProduction;
import io.stackgres.common.crd.sgcluster.StackGresClusterResources;
import io.stackgres.common.crd.sgprofile.StackGresProfile;
import io.stackgres.common.crd.sgprofile.StackGresProfileContainer;
import io.stackgres.common.crd.sgprofile.StackGresProfileRequests;
import io.stackgres.common.crd.sgshardedcluster.StackGresShardedCluster;
import io.stackgres.common.crd.sgshardeddbops.StackGresShardedDbOps;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.operator.conciliation.factory.AbstractProfileDecoratorTestCase;
import io.stackgres.operator.conciliation.factory.cluster.KubernetessMockResourceGenerationUtil;
import io.stackgres.operator.conciliation.shardeddbops.StackGresShardedDbOpsContext;
import org.jooq.lambda.Seq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShardedDbOpsJobContainerProfileDecoratorTest extends AbstractProfileDecoratorTestCase {

  private static final StackGresGroupKind KIND = StackGresGroupKind.DBOPS;

  private final ShardedDbOpsJobContainerProfileDecorator profileDecorator =
      new ShardedDbOpsJobContainerProfileDecorator();

  @Mock
  private StackGresShardedDbOpsContext context;

  private StackGresShardedDbOps dbOps;

  private StackGresShardedCluster cluster;

  private StackGresProfile profile;

  private Job job;

  private List<HasMetadata> resources;

  @BeforeEach
  void setUp() {
    dbOps = Fixtures.shardedDbOps().loadRestart().get();
    cluster = Fixtures.shardedCluster().loadDefault().get();
    profile = Fixtures.instanceProfile().loadSizeS().get();

    final ObjectMeta metadata = dbOps.getMetadata();
    metadata.getAnnotations().put(StackGresContext.VERSION_KEY,
        StackGresProperty.OPERATOR_VERSION.getString());
    resources = KubernetessMockResourceGenerationUtil
        .buildResources(metadata.getName(), metadata.getNamespace());
    job = resources.stream()
        .filter(Job.class::isInstance)
        .map(Job.class::cast)
        .findFirst()
        .orElseThrow();
    profile.getSpec().setContainers(new HashMap<>());
    profile.getSpec().setInitContainers(new HashMap<>());
    Seq.seq(job.getSpec()
            .getTemplate().getSpec().getContainers())
        .forEach(container -> {
          StackGresProfileContainer containerProfile = new StackGresProfileContainer();
          containerProfile.setCpu(new Random().nextInt(32000) + "m");
          containerProfile.setMemory(new Random().nextInt(32) + "Gi");
          profile.getSpec().getContainers().put(
              KIND.getContainerPrefix() + container.getName(), containerProfile);
        });
    Seq.seq(job.getSpec()
            .getTemplate().getSpec().getInitContainers())
        .forEach(container -> {
          StackGresProfileContainer containerProfile = new StackGresProfileContainer();
          containerProfile.setCpu(new Random().nextInt(32000) + "m");
          containerProfile.setMemory(new Random().nextInt(32) + "Gi");
          profile.getSpec().getInitContainers().put(
              KIND.getContainerPrefix() + container.getName(), containerProfile);
        });
    profile.getSpec().setRequests(new StackGresProfileRequests());
    profile.getSpec().getRequests().setCpu(new Random().nextInt(32000) + "m");
    profile.getSpec().getRequests().setMemory(new Random().nextInt(32) + "Gi");
    profile.getSpec().getRequests().setContainers(new HashMap<>());
    profile.getSpec().getRequests().setInitContainers(new HashMap<>());
    Seq.seq(job.getSpec().getTemplate().getSpec().getContainers())
        .forEach(container -> {
          StackGresProfileContainer containerProfile = new StackGresProfileContainer();
          containerProfile.setCpu(new Random().nextInt(32000) + "m");
          containerProfile.setMemory(new Random().nextInt(32) + "Gi");
          profile.getSpec().getRequests().getContainers().put(
              KIND.getContainerPrefix() + container.getName(), containerProfile);
        });
    Seq.seq(job.getSpec().getTemplate().getSpec().getInitContainers())
        .forEach(container -> {
          StackGresProfileContainer containerProfile = new StackGresProfileContainer();
          containerProfile.setCpu(new Random().nextInt(32000) + "m");
          containerProfile.setMemory(new Random().nextInt(32) + "Gi");
          profile.getSpec().getRequests().getInitContainers().put(
              KIND.getContainerPrefix() + container.getName(), containerProfile);
        });
    StackGresProfileContainer containerProfile = new StackGresProfileContainer();
    containerProfile.setCpu(new Random().nextInt(32000) + "m");
    containerProfile.setMemory(new Random().nextInt(32) + "Gi");
    profile.getSpec().getContainers().put(
        KIND.getContainerPrefix() + StringUtil.generateRandom(), containerProfile);
    profile.getSpec().getInitContainers().put(
        KIND.getContainerPrefix() + StringUtil.generateRandom(), containerProfile);

    lenient().when(context.getSource()).thenReturn(dbOps);
    lenient().when(context.getShardedCluster()).thenReturn(cluster);
    lenient().when(context.getProfile()).thenReturn(profile);
  }

  @Override
  protected StackGresProfile getProfile() {
    return profile;
  }

  @Override
  protected PodSpec getPodSpec() {
    return job.getSpec().getTemplate().getSpec();
  }

  @Override
  protected StackGresGroupKind getKind() {
    return KIND;
  }

  @Override
  protected void decorate() {
    resources.forEach(resource -> profileDecorator.decorate(context, resource));
  }

  @Override
  protected void disableResourceRequirements() {
    cluster.getSpec().setNonProductionOptions(new StackGresClusterNonProduction());
    cluster.getSpec().getNonProductionOptions().setDisableClusterResourceRequirements(true);
    when(context.calculateDisableClusterResourceRequirements()).thenReturn(true);
  }

  @Override
  protected void enableLimits() {
    cluster.getSpec().getCoordinator().getPods().setResources(new StackGresClusterResources());
    cluster.getSpec().getCoordinator().getPods().getResources()
        .setEnableClusterLimitsRequirements(true);
  }

}
