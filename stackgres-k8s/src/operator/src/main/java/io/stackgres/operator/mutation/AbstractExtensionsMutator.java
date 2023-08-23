/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.mutation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Predicates;
import io.fabric8.kubernetes.client.CustomResource;
import io.stackgres.common.ExtensionTuple;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterExtension;
import io.stackgres.common.crd.sgcluster.StackGresClusterInstalledExtension;
import io.stackgres.common.extension.ExtensionMetadataManager;
import io.stackgres.common.extension.ExtensionUtil;
import io.stackgres.operatorframework.admissionwebhook.AdmissionReview;
import io.stackgres.operatorframework.admissionwebhook.Operation;
import io.stackgres.operatorframework.admissionwebhook.mutating.Mutator;
import org.jooq.lambda.Seq;

public abstract class AbstractExtensionsMutator<R extends CustomResource<?, ?>,
    T extends AdmissionReview<R>> implements Mutator<R, T> {

  protected abstract ExtensionMetadataManager getExtensionMetadataManager();

  @Override
  public R mutate(T review, R resource) {
    if ((review.getRequest().getOperation() == Operation.CREATE
        || review.getRequest().getOperation() == Operation.UPDATE)
        && extensionsChanged(review)) {
      mutateExtensions(resource);
    }
    return resource;
  }

  protected boolean extensionsChanged(T review) {
    final R resource = review.getRequest().getObject();
    final R oldResource = review.getRequest().getOldObject();
    if (oldResource == null) {
      return true;
    }
    final List<StackGresClusterExtension> extensions = getExtensions(resource);
    final List<StackGresClusterExtension> oldExtensions = getExtensions(oldResource);
    if (!Objects.equals(extensions, oldExtensions)) {
      return true;
    }
    final List<ExtensionTuple> missingDefaultExtensions = getDefaultExtensions(resource);
    final List<ExtensionTuple> oldMissingDefaultExtensions = getDefaultExtensions(oldResource);
    if (!Objects.equals(missingDefaultExtensions, oldMissingDefaultExtensions)) {
      return true;
    }
    final Optional<List<StackGresClusterInstalledExtension>> toInstallExtensions =
        getToInstallExtensions(resource);
    final Optional<List<StackGresClusterInstalledExtension>> oldToInstallExtensions =
        getToInstallExtensions(oldResource);
    if (!Objects.equals(toInstallExtensions, oldToInstallExtensions)) {
      return true;
    }
    return false;
  }

  private void mutateExtensions(R resource) {
    final StackGresCluster cluster = getCluster(resource);
    List<StackGresClusterExtension> extensions = getExtensions(resource);
    List<StackGresClusterInstalledExtension> missingDefaultExtensions =
        getDefaultExtensions(resource).stream()
        .map(t -> t.extensionVersion()
            .map(version -> getExtension(cluster, t.extensionName(), version))
            .orElseGet(() -> getExtension(cluster, t.extensionName())))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(defaultExtension -> extensions.stream()
            .noneMatch(extension -> extension.getName()
                .equals(defaultExtension.getName())))
        .toList();
    final List<StackGresClusterInstalledExtension> toInstallExtensions =
        Seq.seq(extensions)
        .map(extension -> getToInstallExtension(cluster, extension))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .append(missingDefaultExtensions)
        .toList();

    setToInstallExtensions(resource, toInstallExtensions);
    Seq.seq(extensions)
        .forEach(extension -> toInstallExtensions.stream()
            .filter(toInstallExtension -> toInstallExtension.getName()
                .equals(extension.getName()))
            .findFirst()
            .ifPresent(installedExtension -> onExtensionToInstall(
                extension, installedExtension)));
  }

  protected abstract void setToInstallExtensions(R resource,
      List<StackGresClusterInstalledExtension> toInstallExtensions);

  protected abstract Optional<List<StackGresClusterInstalledExtension>> getToInstallExtensions(
      R resource);

  protected abstract StackGresCluster getCluster(R resource);

  protected abstract List<StackGresClusterExtension> getExtensions(R resource);

  protected abstract List<ExtensionTuple> getDefaultExtensions(R resource);

  protected void onExtensionToInstall(
      final StackGresClusterExtension extension,
      final StackGresClusterInstalledExtension installedExtension) {
    if (extension.getVersion() == null
        || !installedExtension.getVersion().equals(extension.getVersion())) {
      extension.setVersion(installedExtension.getVersion());
    }
  }

  protected Optional<StackGresClusterInstalledExtension> getExtension(StackGresCluster cluster,
      String extensionName) {
    StackGresClusterExtension extension = new StackGresClusterExtension();
    extension.setName(extensionName);
    return getExtensionMetadataManager()
        .findExtensionCandidateAnyVersion(cluster, extension, false)
        .map(extensionMetadata -> ExtensionUtil.getInstalledExtension(
            cluster, extension, extensionMetadata, false));
  }

  protected Optional<StackGresClusterInstalledExtension> getExtension(StackGresCluster cluster,
      String extensionName, String extensionVersion) {
    StackGresClusterExtension extension = new StackGresClusterExtension();
    extension.setName(extensionName);
    extension.setVersion(extensionVersion);
    return getExtensionMetadataManager()
        .findExtensionCandidateSameMajorBuild(cluster, extension, false)
        .map(extensionMetadata -> ExtensionUtil.getInstalledExtension(
            cluster, extension, extensionMetadata, false));
  }

  private Optional<StackGresClusterInstalledExtension> getToInstallExtension(
      StackGresCluster cluster, StackGresClusterExtension extension) {
    return getExtensionMetadataManager()
        .findExtensionCandidateSameMajorBuild(cluster, extension, false)
        .or(() -> Optional.of(extension.getVersion() == null)
            .filter(hasNoVersion -> hasNoVersion)
            .map(hasNoVersion -> getExtensionMetadataManager()
                .getExtensionsAnyVersion(cluster, extension, false))
            .filter(Predicates.not(List::isEmpty))
            .filter(allExtensionVersions -> Seq.seq(allExtensionVersions)
                .groupBy(e -> e.getVersion())
                .size() > 1)
            .map(List::stream)
            .flatMap(Stream::findFirst))
        .map(extensionMetadata -> ExtensionUtil.getInstalledExtension(
            cluster, extension, extensionMetadata, false));
  }

}
