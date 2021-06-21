/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.dbops;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.Quantity;
import io.stackgres.common.CdiUtil;
import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsBenchmark;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsPgbench;
import io.stackgres.operator.cluster.factory.ClusterStatefulSetEnvironmentVariables;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.factory.ResourceFactory;
import io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniSecret;
import io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniServices;

@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V09, stopAt = StackGresVersion.V10)
@BenchmarkJob("pgbench")
public class PgbenchBenchmark extends DbOpsJob {

  @Inject
  public PgbenchBenchmark(
      ResourceFactory<StackGresClusterContext, PodSecurityContext> podSecurityFactory,
      ClusterStatefulSetEnvironmentVariables clusterStatefulSetEnvironmentVariables,
      LabelFactory<StackGresCluster> labelFactory,
      JsonMapper jsonMapper) {
    super(podSecurityFactory, clusterStatefulSetEnvironmentVariables, labelFactory, jsonMapper);
  }

  public PgbenchBenchmark() {
    super(null, null, null, null);
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
  }

  @Override
  protected List<EnvVar> getRunEnvVars(StackGresClusterContext context, StackGresDbOps dbOps) {
    StackGresDbOpsBenchmark benchmark = dbOps.getSpec().getBenchmark();
    StackGresDbOpsPgbench pgbench = benchmark.getPgbench();
    final String primaryServiceDns = PatroniServices.readWriteName(context);
    final String serviceDns;
    if (benchmark.isConnectionTypePrimaryService()) {
      serviceDns = primaryServiceDns;
    } else {
      serviceDns = PatroniServices.readOnlyName(context);
    }
    final String scale = Quantity.getAmountInBytes(Quantity.parse(pgbench.getDatabaseSize()))
        .divide(Quantity.getAmountInBytes(Quantity.parse("16Mi")))
        .toPlainString();
    final String duration = String.valueOf(Duration.parse(pgbench.getDuration()).getSeconds());
    List<EnvVar> runEnvVars = ImmutableList.of(
        new EnvVarBuilder()
            .withName("PGHOST")
            .withValue(serviceDns)
            .build(),
        new EnvVarBuilder()
            .withName("PRIMARY_PGHOST")
            .withValue(primaryServiceDns)
            .build(),
        new EnvVarBuilder()
            .withName("PGUSER")
            .withValue("postgres")
            .build(),
        new EnvVarBuilder()
            .withName("PGPASSWORD")
            .withNewValueFrom()
            .withNewSecretKeyRef()
            .withName(PatroniSecret.name(context.getSource()))
            .withKey(PatroniSecret.SUPERUSER_PASSWORD_KEY)
            .endSecretKeyRef()
            .endValueFrom()
            .build(),
        new EnvVarBuilder()
            .withName("SCALE")
            .withValue(scale)
            .build(),
        new EnvVarBuilder()
            .withName("DURATION")
            .withValue(duration)
            .build(),
        new EnvVarBuilder()
            .withName("PROTOCOL")
            .withValue(Optional.of(pgbench)
                .map(StackGresDbOpsPgbench::getUsePreparedStatements)
                .map(usePreparedStatements -> usePreparedStatements ? "prepared" : "simple")
                .orElse("simple"))
            .build(),
        new EnvVarBuilder()
            .withName("READ_WRITE")
            .withValue(Optional.of(benchmark)
                .map(StackGresDbOpsBenchmark::isConnectionTypePrimaryService)
                .map(String::valueOf)
                .orElse("true"))
            .build(),
        new EnvVarBuilder()
            .withName("CLIENTS")
            .withValue(Optional.of(pgbench)
                .map(StackGresDbOpsPgbench::getConcurrentClients)
                .map(String::valueOf)
                .orElse("1"))
            .build(),
        new EnvVarBuilder()
            .withName("JOBS")
            .withValue(Optional.of(pgbench)
                .map(StackGresDbOpsPgbench::getThreads)
                .map(String::valueOf)
                .orElse("1"))
            .build());
    return runEnvVars;
  }

  @Override
  protected ClusterStatefulSetPath getRunScript() {
    return ClusterStatefulSetPath.LOCAL_BIN_RUN_PGBENCH_SH_PATH;
  }

  @Override
  protected ClusterStatefulSetPath getSetResultScript() {
    return ClusterStatefulSetPath.LOCAL_BIN_SET_PGBENCH_RESULT_SH_PATH;
  }
}
