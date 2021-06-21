/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation;

import java.util.Collection;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.common.crd.sgcluster.StackGresCluster;

public interface AnnotationDecorator {

  void decorate(StackGresCluster cluster, Collection<? extends HasMetadata> existingResources,
      Iterable<? extends HasMetadata> resources);

}
