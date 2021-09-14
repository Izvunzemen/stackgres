/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgdbops;

import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresDbOpsMinorVersionUpgrade implements KubernetesResource {

  private static final long serialVersionUID = 1L;

  @JsonProperty("postgresVersion")
  @NotNull
  private String postgresVersion;

  @JsonProperty("method")
  private String method;

  @ReferencedField("method")
  interface Method extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "method must be InPlace or ReducedImpact",
      payload = Method.class)
  public boolean isMethodValid() {
    return method == null
        || ImmutableList.of("InPlace", "ReducedImpact").contains(method);
  }

  @JsonIgnore
  public boolean isMethodReducedImpact() {
    return Objects.equals(method, "ReducedImpact");
  }

  public String getPostgresVersion() {
    return postgresVersion;
  }

  public void setPostgresVersion(String postgresVersion) {
    this.postgresVersion = postgresVersion;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, postgresVersion);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresDbOpsMinorVersionUpgrade)) {
      return false;
    }
    StackGresDbOpsMinorVersionUpgrade other = (StackGresDbOpsMinorVersionUpgrade) obj;
    return Objects.equals(method, other.method)
        && Objects.equals(postgresVersion, other.postgresVersion);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
