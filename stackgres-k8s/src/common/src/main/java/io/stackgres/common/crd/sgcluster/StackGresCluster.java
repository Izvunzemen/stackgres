/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.crd.CommonDefinition;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
@Group(CommonDefinition.GROUP)
@Version(CommonDefinition.VERSION)
@Kind(StackGresCluster.KIND)
public final class StackGresCluster
    extends CustomResource<StackGresClusterSpec, StackGresClusterStatus>
    implements Namespaced {

  private static final long serialVersionUID = -5276087851826599719L;

  public static final String KIND = "SGCluster";

  @JsonProperty("spec")
  @NotNull(message = "The specification is required")
  @Valid
  private StackGresClusterSpec spec;

  @JsonProperty("status")
  @Valid
  private StackGresClusterStatus status;

  public StackGresCluster() {
    super();
  }

  @Override
  public StackGresClusterSpec getSpec() {
    return spec;
  }

  @Override
  public void setSpec(StackGresClusterSpec spec) {
    this.spec = spec;
  }

  @Override
  public StackGresClusterStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(StackGresClusterStatus status) {
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
    if (!(obj instanceof StackGresCluster)) {
      return false;
    }
    StackGresCluster other = (StackGresCluster) obj;
    return Objects.equals(spec, other.spec)
        && Objects.equals(status, other.status);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("kind", getKind())
        .add("apiVersion", getApiVersion())
        .add("metadata", getMetadata())
        .add("spec", spec)
        .add("status", status)
        .toString();
  }
}
