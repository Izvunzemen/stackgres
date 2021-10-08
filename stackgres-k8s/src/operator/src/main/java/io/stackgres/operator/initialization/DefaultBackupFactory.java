/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.initialization;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfig;
import io.stackgres.common.crd.sgbackupconfig.StackGresBackupConfigSpec;

@ApplicationScoped
public class DefaultBackupFactory extends AbstractCustomResourceFactory<StackGresBackupConfig> {

  public static final String BACKUP_DEFAULT_VALUES = "/backup-default-values.properties";
  public static final String NAME = "defaultbackupconfig";

  @Override
  StackGresBackupConfig buildResource(String namespace) {
    StackGresBackupConfig config = new StackGresBackupConfig();

    config.getMetadata().setNamespace(namespace);
    config.getMetadata().setName(generateDefaultName());

    StackGresBackupConfigSpec spec = buildSpec(StackGresBackupConfigSpec.class);
    config.setSpec(spec);
    return config;
  }

  @Override
  Properties getDefaultPropertiesFile() {
    return StackGresUtil.loadProperties(BACKUP_DEFAULT_VALUES);
  }

}
