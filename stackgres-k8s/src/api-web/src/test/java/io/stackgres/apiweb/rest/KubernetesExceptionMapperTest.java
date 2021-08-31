/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.rest;

import javax.ws.rs.core.Response;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.api.model.StatusDetailsBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.stackgres.apiweb.exception.ErrorResponse;
import io.stackgres.apiweb.exception.KubernetesExceptionMapper;
import io.stackgres.apiweb.rest.utils.Kubernetes12StatusParser;
import io.stackgres.apiweb.rest.utils.Kubernetes16StatusParser;
import io.stackgres.common.ErrorType;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KubernetesExceptionMapperTest {

  private KubernetesExceptionMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new KubernetesExceptionMapper(new Kubernetes12StatusParser());
  }

  @Test
  void webHookErrorResponses_shouldBeParsed() {
    final String errorTypeUri = ErrorType.getErrorTypeUri(ErrorType.CONSTRAINT_VIOLATION);
    final String message = "StackGresProfile has invalid properties";
    final String field = "spec.memory";
    final String detail = "spec.memory cannot be empty";
    Status status = new StatusBuilder()
        .withReason(errorTypeUri)
        .withMessage(message)
        .withDetails(new StatusDetailsBuilder()
            .addNewCause(field, detail,
                "NotEmpty")
            .build())
        .withCode(400)
        .build();

    Response response = mapper.toResponse(new KubernetesClientException("error", 400, status));

    ErrorResponse errorResponse = (ErrorResponse) response.getEntity();

    Assertions.assertEquals(errorTypeUri, errorResponse.getType());
    Assertions.assertEquals(ErrorType.CONSTRAINT_VIOLATION.getTitle(), errorResponse.getTitle());
    Assertions.assertEquals(detail, errorResponse.getDetail());
    Assertions.assertEquals(field, errorResponse.getFields()[0]);
  }

  @Test
  void kubernetesValidations_shouldBeParsed() {
    Status status = JsonUtil.readFromJson("kube_status/status-1.13.12.json", Status.class);

    Response response = mapper.toResponse(new KubernetesClientException("error", 422, status));

    ErrorResponse errorResponse = (ErrorResponse) response.getEntity();

    Assertions.assertEquals(ErrorType.getErrorTypeUri(ErrorType.CONSTRAINT_VIOLATION),
        errorResponse.getType());
    Assertions.assertEquals(ErrorType.CONSTRAINT_VIOLATION.getTitle(), errorResponse.getTitle());
    Assertions.assertEquals("spec.memory in body should match '^[0-9]+(\\\\.[0-9]+)?(Mi|Gi)$'",
        errorResponse.getDetail());
    Assertions.assertEquals("spec.memory", errorResponse.getFields()[0]);
  }

  @Test
  void kubernetes16Validations_shouldBeParsed() {
    mapper.setStatusParser(new Kubernetes16StatusParser());

    Status status = JsonUtil.readFromJson("kube_status/status-1.16.4.json", Status.class);

    Response response = mapper.toResponse(new KubernetesClientException("error", 422, status));

    ErrorResponse errorResponse = (ErrorResponse) response.getEntity();

    Assertions.assertEquals(ErrorType.getErrorTypeUri(ErrorType.CONSTRAINT_VIOLATION),
        errorResponse.getType());
    Assertions.assertEquals(ErrorType.CONSTRAINT_VIOLATION.getTitle(), errorResponse.getTitle());
    Assertions.assertEquals("spec.memory in body should match '^[0-9]+(\\\\.[0-9]+)?(Mi|Gi)$'",
        errorResponse.getDetail());
    Assertions.assertEquals("spec.memory", errorResponse.getFields()[0]);
  }

  @Test
  void kubernetesInvalidNameValidation_shouldBeParsed() {
    mapper.setStatusParser(new Kubernetes16StatusParser());

    Status status = JsonUtil.readFromJson("kube_status/invalid_cluster_name.json", Status.class);

    Response response = mapper.toResponse(new KubernetesClientException(status));

    ErrorResponse errorResponse = (ErrorResponse) response.getEntity();

    Assertions.assertEquals(ErrorType.getErrorTypeUri(ErrorType.CONSTRAINT_VIOLATION),
        errorResponse.getType());
    Assertions.assertEquals(ErrorType.CONSTRAINT_VIOLATION.getTitle(), errorResponse.getTitle());
    Assertions.assertEquals(status.getMessage(), errorResponse.getDetail());
    Assertions.assertEquals(0, errorResponse.getFields().length);
  }

  @Test
  void kubernetesAlreadyExists_shouldBeParsed() {
    mapper.setStatusParser(new Kubernetes16StatusParser());

    Status status = JsonUtil.readFromJson("kube_status/already-exists.json", Status.class);

    Response response = mapper.toResponse(new KubernetesClientException("error", 409, status));

    ErrorResponse errorResponse = (ErrorResponse) response.getEntity();

    Assertions.assertEquals(ErrorType.getErrorTypeUri(ErrorType.ALREADY_EXISTS),
        errorResponse.getType());
    Assertions.assertEquals(ErrorType.ALREADY_EXISTS.getTitle(), errorResponse.getTitle());
    Assertions.assertEquals("sgclusters.stackgres.io \\\"minor\\\" already exists",
        errorResponse.getDetail());
    Assertions.assertEquals(0, errorResponse.getFields().length);
  }

}
