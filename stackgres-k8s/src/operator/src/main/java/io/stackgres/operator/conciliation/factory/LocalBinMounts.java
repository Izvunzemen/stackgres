/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.operator.conciliation.VolumeMountProviderName;

@ApplicationScoped
@ProviderName(VolumeMountProviderName.LOCAL_BIN)
public class LocalBinMounts implements VolumeMountsProvider<ContainerContext> {

  @Override
  public List<VolumeMount> getVolumeMounts(ContainerContext context) {
    return List.of(
        new VolumeMountBuilder()
            .withName(PatroniStaticVolume.LOCAL_BIN.getVolumeName())
            .withMountPath(ClusterStatefulSetPath.LOCAL_BIN_PATH.path())
            .build()
    );
  }

  @Override
  public List<EnvVar> getDerivedEnvVars(ContainerContext context) {
    return List.of(
        ClusterStatefulSetPath.LOCAL_BIN_PATH.envVar()
    );
  }
}
