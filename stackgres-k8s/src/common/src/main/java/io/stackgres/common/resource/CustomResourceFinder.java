/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.resource;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public interface CustomResourceFinder<T> {

  /**
   * Will look for a customer resource by it's name in the given namespace.
   * @param name the name of the resource
   * @param namespace the namespace in which the resource should be located
   * @return the result of the search
   */
  @NotNull Optional<T> findByNameAndNamespace(String name, String namespace);

}
