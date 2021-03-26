/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.common;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.operatorframework.resource.factory.OptionalSubResourceStreamFactory;

public interface StackGresClusterOptionalResourceStreamFactory
    extends OptionalSubResourceStreamFactory<HasMetadata, StackGresClusterContext> {

}
