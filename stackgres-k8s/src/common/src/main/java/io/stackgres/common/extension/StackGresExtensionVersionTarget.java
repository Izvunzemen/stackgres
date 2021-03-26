/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.extension;

import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresExtensionVersionTarget {

  @NotNull(message = "postgresVersion is required")
  private String postgresVersion;

  private String postgresExactVersion;

  private String build;

  private String arch;

  private String os;

  public String getPostgresVersion() {
    return postgresVersion;
  }

  public void setPostgresVersion(String postgresVersion) {
    this.postgresVersion = postgresVersion;
  }

  public String getPostgresExactVersion() {
    return postgresExactVersion;
  }

  public void setPostgresExactVersion(String postgresExactVersion) {
    this.postgresExactVersion = postgresExactVersion;
  }

  public String getBuild() {
    return build;
  }

  public void setBuild(String build) {
    this.build = build;
  }

  public String getArch() {
    return arch;
  }

  @JsonIgnore
  public String getArchOrDefault() {
    return Optional.ofNullable(arch).orElse(ExtensionUtil.DEFAULT_ARCH);
  }

  public void setArch(String arch) {
    this.arch = arch;
  }

  public String getOs() {
    return os;
  }

  @JsonIgnore
  public String getOsOrDefault() {
    return Optional.ofNullable(os).orElse(ExtensionUtil.DEFAULT_OS);
  }

  public void setOs(String os) {
    this.os = os;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getArchOrDefault(), build, getOsOrDefault(), postgresVersion,
        postgresExactVersion);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresExtensionVersionTarget)) {
      return false;
    }
    StackGresExtensionVersionTarget other = (StackGresExtensionVersionTarget) obj;
    return Objects.equals(getArchOrDefault(), other.getArchOrDefault())
        && Objects.equals(build, other.build)
        && Objects.equals(getOsOrDefault(), other.getOsOrDefault())
        && Objects.equals(postgresVersion, other.postgresVersion)
        && Objects.equals(postgresExactVersion, other.postgresExactVersion);
  }

}
