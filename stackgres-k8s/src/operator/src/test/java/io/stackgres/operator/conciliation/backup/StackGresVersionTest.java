/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.stackgres.common.StackGresContext;
import io.stackgres.common.StackGresProperty;
import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.operator.common.StackGresVersion;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StackGresVersionTest {

  private StackGresBackup backup;

  @BeforeEach
  void setUp() {
    backup = JsonUtil
        .readFromJson("backup/default.json", StackGresBackup.class);
  }

  @Test
  void givenStackGresValidVersion_shouldNotFail() {
    StackGresVersion.getStackGresVersion(backup);
  }

  @Test
  void givenAValidVersion_shouldReturnTheCorrectStackGresVersion() {
    setStackGresClusterVersion("1.0");

    var version = StackGresVersion.getStackGresVersion(backup);

    assertEquals(StackGresVersion.V10, version);
  }

  @Test
  void givenASnapshotVersion_shouldReturnTheCorrectStackGresVersion() {
    setStackGresClusterVersion("1.0-SNAPSHOT");

    var version = StackGresVersion.getStackGresVersion(backup);

    assertEquals(StackGresVersion.V10, version);
  }

  @Test
  void givenAInvalidVersion_shouldThrowAnException() {
    setStackGresClusterVersion("0.1-SNAPSHOT");

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> StackGresVersion.getStackGresVersion(backup));

    assertEquals("Invalid version 0.1", ex.getMessage());
  }

  @Test
  void givenACurrentVersion_shouldNotFail() {
    setStackGresClusterVersion(StackGresProperty.OPERATOR_VERSION.getString());

    StackGresVersion.getStackGresVersion(backup);
  }

  private void setStackGresClusterVersion(String configVersion) {
    backup.getMetadata().getAnnotations().put(StackGresContext.VERSION_KEY, configVersion);
  }
}
