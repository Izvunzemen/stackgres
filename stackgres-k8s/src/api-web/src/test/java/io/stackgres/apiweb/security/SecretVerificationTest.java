/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.quarkus.security.AuthenticationFailedException;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.resource.ResourceScanner;
import io.stackgres.common.resource.ResourceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecretVerificationTest {

  @Mock
  private ResourceScanner<Secret> secretScanner;

  private Secret secret;

  private SecretVerification secretVerification;

  @BeforeEach
  void setUp() {
    secret = new SecretBuilder()
        .withNewMetadata()
        .withNamespace("stackgres")
        .withName("test")
        .withLabels(ImmutableMap.of(StackGresContext.AUTH_KEY, StackGresContext.AUTH_USER_VALUE))
        .endMetadata()
        .withData(ImmutableMap.of(
            StackGresContext.REST_K8SUSER_KEY, ResourceUtil.encodeSecret("test"),
            StackGresContext.REST_PASSWORD_KEY,
            ResourceUtil.encodeSecret(TokenUtils.sha256("testtest"))))
        .build();
    secretVerification = new SecretVerification();
    secretVerification.setSecretScanner(secretScanner);
    secretVerification.init();
  }

  @Test
  void login_shouldSucceedTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres")).thenReturn(ImmutableList.of(secret));
    assertEquals("test", secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void wrongPasswordLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres")).thenReturn(ImmutableList.of(secret));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "wrong"));
  }

  @Test
  void secretWithoutLabelsLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .editMetadata()
            .withLabels(null)
            .endMetadata()
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithWrongLabelLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .editMetadata()
            .withLabels(ImmutableMap.of(StackGresContext.AUTH_KEY, "wrong"))
            .endMetadata()
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithoutPasswordLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .withData(ImmutableMap.of(
                StackGresContext.REST_K8SUSER_KEY, ResourceUtil.encodeSecret("test")))
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithoutK8sUsernameLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .withData(ImmutableMap.of(
                StackGresContext.REST_PASSWORD_KEY,
                ResourceUtil.encodeSecret(TokenUtils.sha256("testtest"))))
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithEmptyPasswordHashLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .withData(ImmutableMap.of(
                StackGresContext.REST_K8SUSER_KEY, ResourceUtil.encodeSecret("test"),
                StackGresContext.REST_PASSWORD_KEY, ResourceUtil.encodeSecret("")))
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithEmptyK8sUsernameLogin_shouldFailTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .withData(ImmutableMap.of(
                StackGresContext.REST_K8SUSER_KEY, ResourceUtil.encodeSecret(""),
                StackGresContext.REST_PASSWORD_KEY,
                ResourceUtil.encodeSecret(TokenUtils.sha256("testtest"))))
            .build()));
    assertThrows(AuthenticationFailedException.class,
        () -> secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void secretWithApiUsernameLogin_shouldSucceedTest() throws Exception {
    when(secretScanner.findResourcesInNamespace("stackgres"))
        .thenReturn(ImmutableList.of(new SecretBuilder(secret)
            .withData(ImmutableMap.of(
                StackGresContext.REST_K8SUSER_KEY, ResourceUtil.encodeSecret("test2"),
                StackGresContext.REST_APIUSER_KEY, ResourceUtil.encodeSecret("test"),
                StackGresContext.REST_PASSWORD_KEY,
                ResourceUtil.encodeSecret(TokenUtils.sha256("testtest"))))
            .build()));
    assertEquals("test2", secretVerification.verifyCredentials("test", "test"));
  }

  @Test
  void sha256Encoding_shouldSucceedTest() throws Exception {
    String sha256enc = TokenUtils.sha256("testtest");
    assertEquals("37268335dd6931045bdcdf92623ff819a64244b53d0e746d438797349d4da578", sha256enc);
    sha256enc = TokenUtils.sha256("test123");
    assertEquals("ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae", sha256enc);
  }

}
