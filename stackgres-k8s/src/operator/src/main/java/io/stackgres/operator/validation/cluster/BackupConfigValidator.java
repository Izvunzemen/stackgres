/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.cluster;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.stackgres.common.ErrorType;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.resource.CustomResourceFinder;
import io.stackgres.operator.common.StackGresClusterReview;
import io.stackgres.operator.validation.ValidationType;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;

@Singleton
@ValidationType(ErrorType.INVALID_CR_REFERENCE)
public class BackupConfigValidator implements ClusterValidator {

  private final CustomResourceFinder<StackGresBackupConfig> configFinder;

  @Inject
  public BackupConfigValidator(
      CustomResourceFinder<StackGresBackupConfig> configFinder) {
    this.configFinder = configFinder;
  }

  @Override
  public void validate(StackGresClusterReview review) throws ValidationFailed {
    switch (review.getRequest().getOperation()) {
      case CREATE: {
        StackGresCluster cluster = review.getRequest().getObject();
        String backupConfig = cluster.getSpec().getConfiguration().getBackupConfig();
        checkIfBackupConfigExists(review, "Backup config " + backupConfig
            + " not found");
        break;
      }
      case UPDATE: {
        StackGresCluster cluster = review.getRequest().getObject();
        String backupConfig = cluster.getSpec().getConfiguration().getBackupConfig();
        checkIfBackupConfigExists(review, "Cannot update to backup config "
            + backupConfig + " because it doesn't exists");
        break;
      }
      default:
    }

  }

  private void checkIfBackupConfigExists(StackGresClusterReview review,
                                         String onError) throws ValidationFailed {

    StackGresCluster cluster = review.getRequest().getObject();
    String backupConfig = cluster.getSpec().getConfiguration().getBackupConfig();
    String namespace = review.getRequest().getObject().getMetadata().getNamespace();

    if (backupConfig != null) {
      Optional<StackGresBackupConfig> backupConfigOpt = configFinder
          .findByNameAndNamespace(backupConfig, namespace);

      if (!backupConfigOpt.isPresent()) {
        fail(onError);
      }
    }
  }

}
