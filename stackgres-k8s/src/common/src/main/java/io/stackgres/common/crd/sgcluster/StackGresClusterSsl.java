/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.SecretKeySelector;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresClusterSsl {

  @JsonProperty("enabled")
  private Boolean enabled;

  @JsonProperty("certificateSecretKeySelector")
  @Valid
  private SecretKeySelector certificateSecretKeySelector;

  @JsonProperty("privateKeySecretKeySelector")
  @Valid
  private SecretKeySelector privateKeySecretKeySelector;

  @ReferencedField("certificateSecretKeySelector")
  interface CertificateSecretKeySelector extends FieldReference { }

  @ReferencedField("privateKeySecretKeySelector")
  interface SecretKeySecretKeySelector extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "certificateSecretKeySelector is required when enabled is true",
      payload = { CertificateSecretKeySelector.class })
  public boolean isNotEnabledCertificateSecretKeySelectorRequired() {
    return !Optional.ofNullable(enabled).orElse(false) || certificateSecretKeySelector != null;
  }

  @JsonIgnore
  @AssertTrue(message = "privateKeySecretKeySelector is required when enabled is true",
      payload = { SecretKeySecretKeySelector.class })
  public boolean isNotEnabledSecretKeySecretKeySelectorRequired() {
    return !Optional.ofNullable(enabled).orElse(false) || privateKeySecretKeySelector != null;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public SecretKeySelector getCertificateSecretKeySelector() {
    return certificateSecretKeySelector;
  }

  public void setCertificateSecretKeySelector(SecretKeySelector certificateSecretKeySelector) {
    this.certificateSecretKeySelector = certificateSecretKeySelector;
  }

  public SecretKeySelector getPrivateKeySecretKeySelector() {
    return privateKeySecretKeySelector;
  }

  public void setPrivateKeySecretKeySelector(SecretKeySelector privateKeySecretKeySelector) {
    this.privateKeySecretKeySelector = privateKeySecretKeySelector;
  }

  @Override
  public int hashCode() {
    return Objects.hash(certificateSecretKeySelector, enabled, privateKeySecretKeySelector);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresClusterSsl)) {
      return false;
    }
    StackGresClusterSsl other = (StackGresClusterSsl) obj;
    return Objects.equals(certificateSecretKeySelector, other.certificateSecretKeySelector)
        && Objects.equals(enabled, other.enabled)
        && Objects.equals(privateKeySecretKeySelector, other.privateKeySecretKeySelector);
  }

  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
