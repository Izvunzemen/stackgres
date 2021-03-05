/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import java.util.Properties;

import org.jooq.lambda.Unchecked;

public enum StackGresProperty implements StackGresPropertyReader {

  CONTAINER_BUILD("stackgres.containerBuild"),
  OPERATOR_VERSION("stackgres.operatorVersion"),
  OPERATOR_IMAGE_VERSION("stackgres.operatorImageVersion"),
  DOCUMENTATION_URI("stackgres.documentation.uri"),
  DOCUMENTATION_ERRORS_PATH("stackgres.documentation.errorsPath");

  private static final Properties APPLICATION_PROPERTIES =
      Unchecked.supplier(() -> StackGresPropertyReader
          .readApplicationProperties(StackGresProperty.class)).get();

  private final String propertyName;

  StackGresProperty(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public String getEnvironmentVariableName() {
    return name();
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public Properties getApplicationProperties() {
    return APPLICATION_PROPERTIES;
  }

}
