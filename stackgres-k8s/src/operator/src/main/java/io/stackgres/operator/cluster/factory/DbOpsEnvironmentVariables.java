/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.cluster.factory;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.stackgres.common.ClusterStatefulSetEnvVars;
import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.operator.conciliation.dbops.StackGresDbOpsContext;
import io.stackgres.operatorframework.resource.factory.SubResourceStreamFactory;
import org.jooq.lambda.Seq;

@ApplicationScoped
public class DbOpsEnvironmentVariables
    implements SubResourceStreamFactory<EnvVar, StackGresDbOpsContext> {

  public Stream<EnvVar> streamResources(StackGresDbOpsContext context) {
    return Seq.of(ClusterStatefulSetPath.values())
        .map(clusterStatefulSetPath -> clusterStatefulSetPath.envVar(context))
        .append(Seq.of(ClusterStatefulSetEnvVars.values())
            .map(clusterStatefulEnvVar -> clusterStatefulEnvVar.envVar(context.getCluster())));
  }

}
