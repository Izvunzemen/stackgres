/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.backup;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.operator.common.BackupReview;
import io.stackgres.operator.validation.ValidationPipelineTest;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@QuarkusTest
@EnabledIfEnvironmentVariable(named = "QUARKUS_PROFILE", matches = "test")
public class BackupValidationPipelineTest
    extends ValidationPipelineTest<StackGresBackup, BackupReview> {

  @Inject
  public BackupValidationPipeline pipeline;

  @Override
  public BackupReview getConstraintViolatingReview() {
    final BackupReview backupReview = JsonUtil
        .readFromJson("backup_allow_request/create.json", BackupReview.class);
    backupReview.getRequest().getObject().getSpec().setSgCluster(null);
    return backupReview;
  }

  @Override
  public BackupValidationPipeline getPipeline() {
    return pipeline;
  }
}
