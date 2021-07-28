/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.dto.dbops;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class DbOpsPgbenchStatusLatency {

  @JsonProperty("average")
  private DbOpsPgbenchStatusMeasure average;

  @JsonProperty("standardDeviation")
  private DbOpsPgbenchStatusMeasure standardDeviation;

  public DbOpsPgbenchStatusLatency() { }

  public DbOpsPgbenchStatusLatency(DbOpsPgbenchStatusMeasure average,
      DbOpsPgbenchStatusMeasure standardDeviation) {
    this.average = average;
    this.standardDeviation = standardDeviation;
  }

  public DbOpsPgbenchStatusMeasure getAverage() {
    return average;
  }

  public void setAverage(DbOpsPgbenchStatusMeasure average) {
    this.average = average;
  }

  public DbOpsPgbenchStatusMeasure getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(DbOpsPgbenchStatusMeasure standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}
