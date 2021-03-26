/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.dto.storages;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class BackupStorage {

  @JsonProperty("type")
  @NotNull(message = "The storage type is required")
  private String type;

  @JsonProperty("s3")
  @Valid
  private AwsS3Storage s3;

  @JsonProperty("s3Compatible")
  @Valid
  private AwsS3CompatibleStorage s3Compatible;

  @JsonProperty("gcs")
  @Valid
  private GoogleCloudStorage gcs;

  @JsonProperty("azureBlob")
  @Valid
  private AzureBlobStorage azureBlob;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AwsS3Storage getS3() {
    return s3;
  }

  public void setS3(AwsS3Storage s3) {
    this.s3 = s3;
  }

  public AwsS3CompatibleStorage getS3Compatible() {
    return s3Compatible;
  }

  public void setS3Compatible(AwsS3CompatibleStorage s3Compatible) {
    this.s3Compatible = s3Compatible;
  }

  public GoogleCloudStorage getGcs() {
    return gcs;
  }

  public void setGcs(GoogleCloudStorage gcs) {
    this.gcs = gcs;
  }

  public AzureBlobStorage getAzureBlob() {
    return azureBlob;
  }

  public void setAzureBlob(AzureBlobStorage azureBlob) {
    this.azureBlob = azureBlob;
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}
