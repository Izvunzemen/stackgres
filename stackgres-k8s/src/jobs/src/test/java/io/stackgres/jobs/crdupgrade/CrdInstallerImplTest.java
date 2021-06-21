/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.crdupgrade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.stackgres.common.resource.ResourceFinder;
import io.stackgres.common.resource.ResourceWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class CrdInstallerImplTest {

  @Mock
  private ResourceFinder<CustomResourceDefinition> customResourceDefinitionFinder;

  @Mock
  private ResourceWriter<CustomResourceDefinition> customResourceDefinitionWriter;

  private final CrdLoader crdLoader = new MockCustomResourceDefinitionFinder();

  private CrdInstallerImpl crdInstaller;

  @BeforeEach
  void setUp() {
    crdInstaller = new CrdInstallerImpl(customResourceDefinitionFinder,
        customResourceDefinitionWriter, crdLoader);
  }

  @Test
  void installCrd_shouldInstallTheResourceIfDoesNotExists() {
    CustomResourceDefinition definition = crdLoader.scanDefinitions().get(0);
    when(customResourceDefinitionFinder.findByName(definition.getMetadata().getName()))
        .thenReturn(Optional.empty());

    when(customResourceDefinitionWriter.create(any(CustomResourceDefinition.class)))
        .thenReturn(definition);

    crdInstaller.installCrd(definition.getMetadata().getName(), definition.getSpec().getNames().getKind());

    verify(customResourceDefinitionFinder).findByName(definition.getMetadata().getName());
    verify(customResourceDefinitionWriter).create(any(CustomResourceDefinition.class));

  }

  @Test
  void installCrd_shouldPatchTheResourceIfExists() {
    CustomResourceDefinition definition = crdLoader.scanDefinitions().get(0);
    when(customResourceDefinitionFinder.findByName(definition.getMetadata().getName()))
        .thenAnswer((Answer<Optional<CustomResourceDefinition>>) invocationOnMock ->
            Optional.of(crdLoader.load(definition.getSpec().getNames().getKind())));

    when(customResourceDefinitionWriter.update(any(CustomResourceDefinition.class)))
        .thenReturn(definition);

    crdInstaller.installCrd(definition.getMetadata().getName(), definition.getSpec().getNames().getKind());

    verify(customResourceDefinitionFinder).findByName(definition.getMetadata().getName());
    verify(customResourceDefinitionWriter).update(any(CustomResourceDefinition.class));

  }
}