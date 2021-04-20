/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgbackup;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;
import org.jooq.lambda.Seq;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class StackGresBackupProcess {

  private String status;
  private String jobPod;
  private String failure;
  private Boolean managedLifecycle;

  @Valid
  private StackgresBackupTiming timing;

  @ReferencedField("status")
  interface Status extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "status must be one of Pending, Running, Completed or Failed",
      payload = { Status.class })
  public boolean isValidStatus() {
    return status != null && Seq.of(BackupPhase.values())
        .map(BackupPhase::label)
        .anyMatch(status::equals);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getJobPod() {
    return jobPod;
  }

  public void setJobPod(String jobPod) {
    this.jobPod = jobPod;
  }

  public String getFailure() {
    return failure;
  }

  public void setFailure(String failure) {
    this.failure = failure;
  }

  public StackgresBackupTiming getTiming() {
    return timing;
  }

  public void setTiming(StackgresBackupTiming timing) {
    this.timing = timing;
  }

  public Boolean getManagedLifecycle() {
    return managedLifecycle;
  }

  public void setManagedLifecycle(Boolean managedLifecycle) {
    this.managedLifecycle = managedLifecycle;
  }

  @Override
  public int hashCode() {
    return Objects.hash(failure, jobPod, managedLifecycle, status, timing);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresBackupProcess)) {
      return false;
    }
    StackGresBackupProcess other = (StackGresBackupProcess) obj;
    return Objects.equals(failure, other.failure) && Objects.equals(jobPod, other.jobPod)
        && Objects.equals(managedLifecycle, other.managedLifecycle)
        && Objects.equals(status, other.status) && Objects.equals(timing, other.timing);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
