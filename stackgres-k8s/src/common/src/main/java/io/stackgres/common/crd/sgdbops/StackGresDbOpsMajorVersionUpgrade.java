/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgdbops;

import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresDbOpsMajorVersionUpgrade implements KubernetesResource {

  private static final long serialVersionUID = 1L;

  @JsonProperty("postgresVersion")
  @NotNull
  private String postgresVersion;

  @JsonProperty("sgPostgresConfig")
  @NotNull
  private String sgPostgresConfig;

  @JsonProperty("link")
  private Boolean link;

  @JsonProperty("clone")
  private Boolean clone;

  @JsonProperty("check")
  private Boolean check;

  @ReferencedField("link")
  interface Link extends FieldReference { }

  @ReferencedField("clone")
  interface Clone extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "link and clone are mutually exclusive",
      payload = { Link.class, Clone.class })
  public boolean isOnlyLinkOrOnlyClone() {
    return !Optional.ofNullable(link).orElse(false)
        || !Optional.ofNullable(clone).orElse(false);
  }

  public String getPostgresVersion() {
    return postgresVersion;
  }

  public void setPostgresVersion(String postgresVersion) {
    this.postgresVersion = postgresVersion;
  }

  public String getSgPostgresConfig() {
    return sgPostgresConfig;
  }

  public void setSgPostgresConfig(String sgPostgresConfig) {
    this.sgPostgresConfig = sgPostgresConfig;
  }

  public Boolean getLink() {
    return link;
  }

  public void setLink(Boolean link) {
    this.link = link;
  }

  public Boolean getClone() {
    return clone;
  }

  public void setClone(Boolean clone) {
    this.clone = clone;
  }

  public Boolean getCheck() {
    return check;
  }

  public void setCheck(Boolean check) {
    this.check = check;
  }

  @Override
  public int hashCode() {
    return Objects.hash(check, clone, link, postgresVersion, sgPostgresConfig);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresDbOpsMajorVersionUpgrade)) {
      return false;
    }
    StackGresDbOpsMajorVersionUpgrade other = (StackGresDbOpsMajorVersionUpgrade) obj;
    return Objects.equals(check, other.check) && Objects.equals(clone, other.clone)
        && Objects.equals(link, other.link)
        && Objects.equals(postgresVersion, other.postgresVersion)
        && Objects.equals(sgPostgresConfig, other.sgPostgresConfig);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
