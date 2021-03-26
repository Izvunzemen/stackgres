/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.common;

import io.stackgres.operatorframework.resource.factory.ContainerResourceFactory;

public interface StackGresClusterSidecarResourceFactory<T>
    extends StackGresClusterResourceStreamFactory,
      ContainerResourceFactory<T, StackGresClusterContext> {

}
