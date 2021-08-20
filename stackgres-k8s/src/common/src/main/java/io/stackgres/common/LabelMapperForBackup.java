/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import io.fabric8.kubernetes.client.CustomResource;

public interface LabelMapperForBackup<T extends CustomResource<?, ?>>
    extends LabelMapper<T> {

  default String backupKey() {
    return StackGresContext.BACKUP_KEY;
  }

}
