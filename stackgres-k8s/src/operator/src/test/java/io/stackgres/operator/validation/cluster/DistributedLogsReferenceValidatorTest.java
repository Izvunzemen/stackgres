/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import io.stackgres.common.resource.AbstractCustomResourceFinder;
import io.stackgres.operator.common.StackGresClusterReview;
import io.stackgres.operatorframework.admissionwebhook.Operation;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class DistributedLogsReferenceValidatorTest {

  private DistributedLogsValidator validator;

  @Mock
  private AbstractCustomResourceFinder<StackGresDistributedLogs> distributedLogsFinder;

  private StackGresDistributedLogs distributedLogs;

  @BeforeEach
  void setUp() throws Exception {
    validator = new DistributedLogsValidator(distributedLogsFinder);

    distributedLogs = JsonUtil.readFromJson("distributedlogs/default.json",
        StackGresDistributedLogs.class);

  }

  @Test
  void givenValidStackGresReferenceOnCreation_shouldNotFail() throws ValidationFailed {

    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation.json", StackGresClusterReview.class);

    String distributedLogsName =
        review.getRequest().getObject().getSpec().getDistributedLogs().getDistributedLogs();
    String namespace = review.getRequest().getObject().getMetadata().getNamespace();

    when(distributedLogsFinder.findByNameAndNamespace(distributedLogsName, namespace))
        .thenReturn(Optional.of(distributedLogs));

    validator.validate(review);

    verify(distributedLogsFinder).findByNameAndNamespace(eq(distributedLogsName), eq(namespace));

  }

  @Test
  void giveInvalidStackGresReferenceOnCreation_shouldFail() {

    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation.json", StackGresClusterReview.class);

    String distributedLogsName =
        review.getRequest().getObject().getSpec().getDistributedLogs().getDistributedLogs();
    String namespace = review.getRequest().getObject().getMetadata().getNamespace();

    when(distributedLogsFinder.findByNameAndNamespace(distributedLogsName, namespace))
        .thenReturn(Optional.empty());

    ValidationFailed ex = assertThrows(ValidationFailed.class, () -> {
      validator.validate(review);
    });

    String resultMessage = ex.getMessage();

    assertEquals("Distributed logs " + distributedLogsName + " not found", resultMessage);

    verify(distributedLogsFinder).findByNameAndNamespace(anyString(), anyString());
  }

  @Test
  void giveAnAttemptToUpdateToAnUnknownProfile_shouldFail() {

    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/distributed_logs_update.json",
            StackGresClusterReview.class);

    String distributedLogsName =
        review.getRequest().getObject().getSpec().getDistributedLogs().getDistributedLogs();
    String namespace = review.getRequest().getObject().getMetadata().getNamespace();

    when(distributedLogsFinder.findByNameAndNamespace(distributedLogsName, namespace))
        .thenReturn(Optional.empty());

    ValidationFailed ex = assertThrows(ValidationFailed.class, () -> {
      validator.validate(review);
    });

    String resultMessage = ex.getMessage();

    assertEquals("Distributed logs " + distributedLogsName + " not found", resultMessage);

    verify(distributedLogsFinder).findByNameAndNamespace(anyString(), anyString());

  }

  @Test
  void giveAnAttemptToUpdateToAnKnownProfile_shouldNotFail() throws ValidationFailed {

    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/distributed_logs_update.json",
            StackGresClusterReview.class);

    String distributedLogsName =
        review.getRequest().getObject().getSpec().getDistributedLogs().getDistributedLogs();
    String namespace = review.getRequest().getObject().getMetadata().getNamespace();

    StackGresDistributedLogs distributedLogs = JsonUtil.readFromJson("distributedlogs/default.json",
        StackGresDistributedLogs.class);

    when(distributedLogsFinder.findByNameAndNamespace(distributedLogsName, namespace))
        .thenReturn(Optional.of(distributedLogs));

    validator.validate(review);

    verify(distributedLogsFinder).findByNameAndNamespace(anyString(), anyString());

  }

  @Test
  void giveAnAttemptToDelete_shouldNotFail() throws ValidationFailed {

    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/distributed_logs_update.json",
            StackGresClusterReview.class);
    review.getRequest().setOperation(Operation.DELETE);

    validator.validate(review);

    verify(distributedLogsFinder, never()).findByNameAndNamespace(anyString(), anyString());

  }

}
