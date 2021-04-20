/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgbackup;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.CommonDefinition;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
@Group(CommonDefinition.GROUP)
@Version(CommonDefinition.VERSION)
@Kind(StackGresBackup.KIND)
public final class StackGresBackup
    extends CustomResource<StackGresBackupSpec, StackGresBackupStatus>
    implements Namespaced {

  private static final long serialVersionUID = 8062109585634644327L;

  public static final String KIND = "SGBackup";

  @JsonProperty("spec")
  @NotNull(message = "The specification is required")
  @Valid
  private StackGresBackupSpec spec;

  @JsonProperty("status")
  @Valid
  private StackGresBackupStatus status;

  public StackGresBackup() {
    super();
  }

  @Override
  public StackGresBackupSpec getSpec() {
    return spec;
  }

  @Override
  public void setSpec(StackGresBackupSpec spec) {
    this.spec = spec;
  }

  @Override
  public StackGresBackupStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(StackGresBackupStatus status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, status);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresBackup)) {
      return false;
    }
    StackGresBackup other = (StackGresBackup) obj;
    return Objects.equals(spec, other.spec) && Objects.equals(status, other.status);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
