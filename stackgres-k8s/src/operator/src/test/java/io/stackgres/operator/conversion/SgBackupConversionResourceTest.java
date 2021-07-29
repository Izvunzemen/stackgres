/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conversion;

import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SgBackupConversionResourceTest extends ConversionResourceTest<StackGresBackup> {

  @Override
  protected StackGresBackup getCustomResource() {
    return JsonUtil.readFromJson("stackgres_backup/default.json", StackGresBackup.class);
  }

  @Override
  protected ConversionResource getConversionResource() {
    return new SgBackupConversionResource(pipeline);
  }
}
