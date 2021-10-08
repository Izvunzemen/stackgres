/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.mutation.pgconfig;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.google.common.collect.ImmutableList;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfig;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfigSpec;
import io.stackgres.operator.common.PgConfigReview;
import io.stackgres.operator.mutation.DefaultValuesMutator;

public class PgConfigDefaultValuesMutator
    extends DefaultValuesMutator<StackGresPostgresConfig, PgConfigReview>
    implements PgConfigMutator {

  @Override
  public JsonNode getTargetNode(StackGresPostgresConfig resource) {
    return super.getTargetNode(resource)
        .get("spec").get("postgresql.conf");
  }

  @Override
  public List<JsonPatchOperation> mutate(PgConfigReview review) {

    ImmutableList.Builder<JsonPatchOperation> operations = ImmutableList.builder();

    StackGresPostgresConfig pgConfig = review.getRequest().getObject();
    StackGresPostgresConfigSpec spec = pgConfig.getSpec();
    if (spec == null) {
      spec = new StackGresPostgresConfigSpec();
      pgConfig.setSpec(spec);
      operations.add(new AddOperation(PG_CONFIG_POINTER.parent(), FACTORY.objectNode()));
    }
    if (spec.getPostgresqlConf() == null) {
      pgConfig.getSpec().setPostgresqlConf(Map.of());
      operations.add(new AddOperation(PG_CONFIG_POINTER, FACTORY.objectNode()));
    }

    operations.addAll(mutate(PG_CONFIG_POINTER, pgConfig));
    return operations.build();

  }
}
