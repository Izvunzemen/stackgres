/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.patroni.factory;

import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleRefBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.crd.CommonDefinition;
import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;
import io.stackgres.common.crd.sgprofile.StackGresProfile;
import io.stackgres.operator.common.LabelFactoryDelegator;
import io.stackgres.operator.common.StackGresClusterContext;
import io.stackgres.operator.common.StackGresClusterResourceStreamFactory;
import io.stackgres.operator.common.StackGresGeneratorContext;
import io.stackgres.operatorframework.resource.ResourceUtil;
import org.jooq.lambda.Seq;

@ApplicationScoped
public class PatroniRole implements StackGresClusterResourceStreamFactory {

  public static final String SUFFIX = "-patroni";

  private LabelFactoryDelegator factoryDelegator;

  public static String roleName(StackGresClusterContext clusterContext) {
    return roleName(clusterContext.getCluster().getMetadata().getName());
  }

  public static String roleName(String clusterName) {
    return ResourceUtil.resourceName(clusterName + SUFFIX);
  }

  @Override
  public Stream<HasMetadata> streamResources(StackGresGeneratorContext context) {
    return Seq.of(
        createServiceAccount(context),
        createRole(context),
        createRoleBinding(context));
  }

  /**
   * Create the ServiceAccount for patroni associated to the cluster.
   */
  private ServiceAccount createServiceAccount(StackGresGeneratorContext context) {
    final StackGresClusterContext clusterContext = context.getClusterContext();
    final StackGresCluster cluster = clusterContext.getCluster();
    final LabelFactory<?> labelFactory = factoryDelegator.pickFactory(clusterContext);
    final Map<String, String> labels = labelFactory
        .clusterLabels(cluster);
    return new ServiceAccountBuilder()
        .withNewMetadata()
        .withName(roleName(clusterContext))
        .withNamespace(cluster.getMetadata().getNamespace())
        .withLabels(labels)
        .withOwnerReferences(clusterContext.getOwnerReferences())
        .endMetadata()
        .build();
  }

  /**
   * Create the Role for patroni associated to the cluster.
   */
  private Role createRole(StackGresGeneratorContext context) {
    final StackGresClusterContext clusterContext = context.getClusterContext();
    final StackGresCluster cluster = clusterContext.getCluster();
    final Map<String, String> labels = factoryDelegator.pickFactory(clusterContext)
        .clusterLabels(cluster);
    return new RoleBuilder()
        .withNewMetadata()
        .withName(roleName(clusterContext))
        .withNamespace(cluster.getMetadata().getNamespace())
        .withLabels(labels)
        .withOwnerReferences(clusterContext.getOwnerReferences())
        .endMetadata()
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("endpoints", "configmaps")
            .withVerbs("create", "get", "list", "patch", "update", "watch")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("secrets")
            .withVerbs("get")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("pods")
            .withVerbs("get", "list", "patch", "update", "watch")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("pods/exec")
            .withVerbs("create")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("batch")
            .withResources("cronjobs")
            .withVerbs("get", "patch")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("services")
            .withVerbs("create")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups("")
            .withResources("events")
            .withVerbs("get", "list", "create", "patch", "update")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups(CommonDefinition.GROUP)
            .withResources(CustomResource.getPlural(StackGresBackup.class))
            .withVerbs("list", "get", "create", "patch", "delete")
            .build())
        .addToRules(new PolicyRuleBuilder()
            .withApiGroups(CommonDefinition.GROUP)
            .withResources(
                CustomResource.getPlural(StackGresBackupConfig.class),
                CustomResource.getPlural(StackGresCluster.class),
                CustomResource.getPlural(StackGresPostgresConfig.class),
                CustomResource.getPlural(StackGresPoolingConfig.class),
                CustomResource.getPlural(StackGresProfile.class),
                CustomResource.getPlural(StackGresDistributedLogs.class),
                CustomResource.getPlural(StackGresDbOps.class))
            .withVerbs("get", "list", "watch", "patch")
            .build())
        .build();
  }

  /**
   * Create the RoleBinding for patroni associated to the cluster.
   */
  private RoleBinding createRoleBinding(StackGresGeneratorContext context) {
    final StackGresClusterContext clusterContext = context.getClusterContext();
    final StackGresCluster cluster = clusterContext.getCluster();
    final LabelFactory<?> labelFactory = factoryDelegator.pickFactory(clusterContext);
    final Map<String, String> labels = labelFactory
        .clusterLabels(cluster);
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(roleName(clusterContext))
        .withNamespace(cluster.getMetadata().getNamespace())
        .withLabels(labels)
        .withOwnerReferences(clusterContext.getOwnerReferences())
        .endMetadata()
        .withSubjects(new SubjectBuilder()
            .withKind("ServiceAccount")
            .withName(roleName(clusterContext))
            .withNamespace(cluster.getMetadata().getNamespace())
            .build())
        .withRoleRef(new RoleRefBuilder()
            .withKind("Role")
            .withName(roleName(clusterContext))
            .withApiGroup("rbac.authorization.k8s.io")
            .build())
        .build();
  }

  @Inject
  public void setFactoryDelegator(LabelFactoryDelegator factoryDelegator) {
    this.factoryDelegator = factoryDelegator;
  }
}
