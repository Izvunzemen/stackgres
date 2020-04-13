/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.pgconfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.api.model.StatusDetailsBuilder;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfigDefinition;
import io.stackgres.common.crd.sgpgconfig.StackGresPostgresConfigSpec;
import io.stackgres.operator.common.ConfigContext;
import io.stackgres.operator.common.ErrorType;
import io.stackgres.operator.common.PgConfigReview;
import io.stackgres.operator.validation.ValidationType;
import io.stackgres.operatorframework.admissionwebhook.Operation;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;

@Singleton
@ValidationType(ErrorType.FORBIDDEN_CR_UPDATE)
public class PgConfigUpdateValidator implements PgConfigValidator {

  private final ConfigContext context;

  private String pgVersionPath;

  @Inject
  public PgConfigUpdateValidator(ConfigContext context) {
    this.context = context;
  }

  @PostConstruct
  public void init() throws NoSuchFieldException {

    String pgVersionJsonField = StackGresPostgresConfigSpec.class
        .getDeclaredField("postgresVersion")
        .getAnnotation(JsonProperty.class).value();

    this.pgVersionPath = "spec." + pgVersionJsonField;
  }

  @Override
  public void validate(PgConfigReview review) throws ValidationFailed {

    if (review.getRequest().getOperation() == Operation.UPDATE) {
      String oldPgVersion = review.getRequest().getOldObject().getSpec().getPostgresVersion();
      String newPgVersion = review.getRequest().getObject().getSpec().getPostgresVersion();

      if (!oldPgVersion.equals(newPgVersion)) {
        String detail = "postgresVersion is not updatable";

        Status failedStatus = new StatusBuilder()
            .withCode(400)
            .withKind(StackGresPostgresConfigDefinition.KIND)
            .withReason(context.getErrorTypeUri(ErrorType.FORBIDDEN_CR_UPDATE))
            .withDetails(new StatusDetailsBuilder()
                .addNewCause(pgVersionPath, detail, "FieldNotUpdatable")
                .withKind(StackGresPostgresConfigDefinition.KIND)
                .withGroup(StackGresContext.CRD_GROUP)
                .withName(pgVersionPath)
                .build())
            .build();

        throw new ValidationFailed(failedStatus);
      }
    }

  }
}
