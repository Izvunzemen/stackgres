/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import io.stackgres.common.StackGresVersion.StackGresMinorVersion;
import io.stackgres.common.component.Component;
import io.stackgres.common.component.Components;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgdistributedlogs.StackGresDistributedLogs;
import org.jooq.lambda.tuple.Tuple;

public enum StackGresComponent {

  POSTGRESQL,
  BABELFISH,
  PATRONI,
  POSTGRES_UTIL,
  PGBOUNCER,
  PROMETHEUS_POSTGRES_EXPORTER,
  ENVOY,
  FLUENT_BIT,
  FLUENTD,
  KUBECTL;

  public static final String LATEST = "latest";

  final Map<StackGresMinorVersion, Component> componentMap;

  StackGresComponent() {
    ImmutableMap.Builder<StackGresMinorVersion, Component> componentMapBuilder =
        ImmutableMap.builder();
    Stream.of(Components.values())
        .flatMap(cs -> Stream.of(cs)
            .map(c -> c.getComponent(this))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(c -> Tuple.tuple(cs.getVersion(), c)))
        .forEach(component -> componentMapBuilder.put(component.v1, component.v2));
    this.componentMap = componentMapBuilder.build();
  }

  public Component getLatest() {
    return getOrThrow(StackGresMinorVersion.LATEST);
  }

  public boolean has(StackGresCluster cluster) {
    return get(StackGresVersion.getStackGresVersion(cluster).getMinorVersion()).isPresent();
  }

  public Component get(StackGresCluster cluster) {
    return getOrThrow(StackGresVersion.getStackGresVersion(cluster));
  }

  public Component get(StackGresDistributedLogs distributedLogs) {
    return getOrThrow(StackGresVersion.getStackGresVersion(distributedLogs));
  }

  public Optional<Component> get(StackGresMinorVersion version) {
    return Optional.of(this.componentMap)
        .map(map -> map.get(version));
  }

  public Component getOrThrow(StackGresVersion version) {
    return getOrThrow(version.getMinorVersion());
  }

  private Component getOrThrow(StackGresMinorVersion version) {
    return get(version)
        .orElseThrow(() -> new IllegalArgumentException(
            "StackGres minor version " + version + " not supported"));
  }

  public Map<StackGresMinorVersion, Component> getComponentVersions() {
    return componentMap;
  }

}
