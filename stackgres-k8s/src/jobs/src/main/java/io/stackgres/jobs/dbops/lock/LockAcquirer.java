/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops.lock;

import java.util.function.Consumer;

import io.fabric8.kubernetes.client.CustomResource;

public interface LockAcquirer<T extends CustomResource<?, ?>> {

  void lockRun(LockRequest target, Consumer<T> tasks);

}
