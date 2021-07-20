/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;
import io.stackgres.common.KubernetesClientFactory;
import io.stackgres.common.StringUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConfigMapResourceTest implements AuthenticatedResourceTest {

  @Inject
  KubernetesClientFactory factory;

  @BeforeEach
  void setUp() {
    try (KubernetesClient client = factory.create()) {
      client.configMaps().inNamespace("test").delete();
    }
  }

  @Test
  void ifNoConfigMapsAreCreated_itShouldReturnAnEmptyArray() {
    given()
        .when()
        .header(AUTHENTICATION_HEADER)
        .get("/stackgres/namespaces/test/configmaps")
        .then().statusCode(200)
        .body("", Matchers.hasSize(0));
  }

  @Test
  void ifConfigMapsAreCreated_itShouldReturnThenInAnArray() {

    final String randomPlainValue = StringUtil.generateRandom();

    try (KubernetesClient client = factory.create()) {
      client.configMaps().inNamespace("test")
          .create(new ConfigMapBuilder()
              .withData(ImmutableMap.of("testKey", randomPlainValue))
              .withNewMetadata()
              .withName("testConfigMaps")
              .endMetadata()
              .build());
    }

    given()
        .when()
        .header(AUTHENTICATION_HEADER)
        .get("/stackgres/namespaces/test/configmaps")
        .then().statusCode(200)
        .body("", Matchers.hasSize(1))
        .body("[0].data.testKey", is(randomPlainValue))
        .body("[0].metadata.name", is("testConfigMaps"));

  }
}