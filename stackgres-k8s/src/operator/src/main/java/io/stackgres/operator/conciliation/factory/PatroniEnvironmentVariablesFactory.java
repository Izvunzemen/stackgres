/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory;

import static io.stackgres.common.patroni.StackGresRandomPasswordKeys.AUTHENTICATOR_PASSWORD_KEY;
import static io.stackgres.common.patroni.StackGresRandomPasswordKeys.REPLICATION_PASSWORD_KEY;
import static io.stackgres.common.patroni.StackGresRandomPasswordKeys.RESTAPI_PASSWORD_KEY;
import static io.stackgres.common.patroni.StackGresRandomPasswordKeys.SUPERUSER_PASSWORD_KEY;

import java.util.List;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectFieldSelectorBuilder;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import io.stackgres.common.EnvoyUtil;

public abstract class PatroniEnvironmentVariablesFactory<T>
    implements ResourceFactory<T, List<EnvVar>> {

  protected List<EnvVar> createPatroniEnvVars(HasMetadata cluster) {
    return List.of(
        new EnvVarBuilder()
            .withName("PATRONI_RESTAPI_CONNECT_ADDRESS")
            .withValue("${PATRONI_KUBERNETES_POD_IP}:" + EnvoyUtil.PATRONI_ENTRY_PORT)
            .build(),
        new EnvVarBuilder()
            .withName("PATRONI_RESTAPI_USERNAME")
            .withValue("superuser")
            .build(),
        new EnvVarBuilder()
            .withName("PATRONI_RESTAPI_PASSWORD")
            .withValueFrom(new EnvVarSourceBuilder()
                .withSecretKeyRef(
                    new SecretKeySelectorBuilder()
                        .withName(cluster.getMetadata().getName())
                        .withKey(RESTAPI_PASSWORD_KEY)
                        .build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_NAME")
            .withValueFrom(new EnvVarSourceBuilder()
                .withFieldRef(
                    new ObjectFieldSelectorBuilder()
                        .withFieldPath("metadata.name").build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_KUBERNETES_NAMESPACE")
            .withValueFrom(new EnvVarSourceBuilder()
                .withFieldRef(
                    new ObjectFieldSelectorBuilder()
                        .withFieldPath("metadata.namespace")
                        .build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_KUBERNETES_POD_IP")
            .withValueFrom(
                new EnvVarSourceBuilder()
                    .withFieldRef(
                        new ObjectFieldSelectorBuilder()
                            .withFieldPath("status.podIP")
                            .build())
                    .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_SUPERUSER_PASSWORD")
            .withValueFrom(new EnvVarSourceBuilder()
                .withSecretKeyRef(
                    new SecretKeySelectorBuilder()
                        .withName(cluster.getMetadata().getName())
                        .withKey(SUPERUSER_PASSWORD_KEY)
                        .build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_REPLICATION_PASSWORD")
            .withValueFrom(new EnvVarSourceBuilder()
                .withSecretKeyRef(
                    new SecretKeySelectorBuilder()
                        .withName(cluster.getMetadata().getName())
                        .withKey(REPLICATION_PASSWORD_KEY)
                        .build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_authenticator_PASSWORD")
            .withValueFrom(new EnvVarSourceBuilder()
                .withSecretKeyRef(
                    new SecretKeySelectorBuilder()
                        .withName(cluster.getMetadata().getName())
                        .withKey(AUTHENTICATOR_PASSWORD_KEY)
                        .build())
                .build())
            .build(),
        new EnvVarBuilder().withName("PATRONI_authenticator_OPTIONS")
            .withValue("superuser")
            .build());

  }
}
