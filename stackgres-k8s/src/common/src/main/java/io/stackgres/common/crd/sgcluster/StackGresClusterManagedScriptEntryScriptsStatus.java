/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresClusterManagedScriptEntryScriptsStatus {

  @JsonProperty("id")
  @NotNull(message = "id can not be null")
  private Integer id;

  @JsonProperty("version")
  @NotNull(message = "version can not be null")
  private Integer version;

  @JsonProperty("failureCode")
  private String failureCode;

  @JsonProperty("failure")
  private String failure;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public void setFailureCode(String failureCode) {
    this.failureCode = failureCode;
  }

  public String getFailure() {
    return failure;
  }

  public void setFailure(String failure) {
    this.failure = failure;
  }

  @Override
  public int hashCode() {
    return Objects.hash(failure, failureCode, id, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresClusterManagedScriptEntryScriptsStatus)) {
      return false;
    }
    StackGresClusterManagedScriptEntryScriptsStatus other =
        (StackGresClusterManagedScriptEntryScriptsStatus) obj;
    return Objects.equals(failure, other.failure) && Objects.equals(failureCode, other.failureCode)
        && Objects.equals(id, other.id) && Objects.equals(version, other.version);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
