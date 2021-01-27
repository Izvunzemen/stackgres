/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operatorframework.reconciliation;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.stackgres.operatorframework.resource.ResourceHandlerContext;
import io.stackgres.operatorframework.resource.ResourceHandlerSelector;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReconciliator<T extends ResourceHandlerContext,
    H extends CustomResource, S extends ResourceHandlerSelector<T>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReconciliator.class);

  protected final String name;
  protected final S handlerSelector;
  protected final KubernetesClient client;
  protected final ObjectMapper objectMapper;
  protected final T context;
  protected final H contextResource;

  protected AbstractReconciliator(String name, S handlerSelector,
      KubernetesClient client, ObjectMapper objectMapper, T context, H contextResource) {
    super();
    this.name = name;
    this.handlerSelector = handlerSelector;
    this.client = client;
    this.objectMapper = objectMapper;
    this.context = context;
    this.contextResource = contextResource;
  }

  private enum Operation {
    NONE,
    CREATED,
    UPDATED
  }

  void reconcile() {
    onPreConfigReconcilied();

    deleteUnwantedResources();
    Operation result = createOrUpdateRequiredResources();

    if (result == Operation.UPDATED) {
      LOGGER.info(name + " updated: '{}.{}'",
          contextResource.getMetadata().getNamespace(),
          contextResource.getMetadata().getName());
      onConfigUpdated();
    }

    if (result == Operation.CREATED) {
      LOGGER.info(name + " created: '{}.{}'",
          contextResource.getMetadata().getNamespace(),
          contextResource.getMetadata().getName());
      onConfigCreated();
    }

    onPostConfigReconcilied();

    LOGGER.debug(name + " synced: '{}.{}'",
        contextResource.getMetadata().getNamespace(),
        contextResource.getMetadata().getName());
  }

  private void deleteUnwantedResources() {
    for (Tuple2<HasMetadata, Optional<HasMetadata>> existingResource :
        context.getExistingResources()) {
      final ImmutableMap<String, String> labels = context.getLabels();
      if (!labels.entrySet().stream().allMatch(
          entry -> Objects.equals(entry.getValue(),
              existingResource.v1.getMetadata().getLabels().get(entry.getKey())))) {
        if (handlerSelector.skipDeletion(context, existingResource.v1)) {
          LOGGER.trace("Skip deletion of resource {}.{} of type {}",
              existingResource.v1.getMetadata().getNamespace(),
              existingResource.v1.getMetadata().getName(),
              existingResource.v1.getKind());
          continue;
        }
        LOGGER.debug("Deleting resource {}.{} of type {}"
            + " since does not belong to existing " + name,
            existingResource.v1.getMetadata().getNamespace(),
            existingResource.v1.getMetadata().getName(),
            existingResource.v1.getKind());
        handlerSelector.delete(client, context, existingResource.v1);
      } else if (!existingResource.v2.isPresent()
          && !handlerSelector.isManaged(context, existingResource.v1)) {
        if (handlerSelector.skipDeletion(context, existingResource.v1)) {
          LOGGER.trace("Skip deletion of resource {}.{} of type {}",
              existingResource.v1.getMetadata().getNamespace(),
              existingResource.v1.getMetadata().getName(),
              existingResource.v1.getKind());
          continue;
        }
        LOGGER.debug("Deleting resource {}.{} of type {}"
            + " since does not belong to existing " + name,
            existingResource.v1.getMetadata().getNamespace(),
            existingResource.v1.getMetadata().getName(),
            existingResource.v1.getKind());
        handlerSelector.delete(client, context, existingResource.v1);
      }
    }
  }

  private Operation createOrUpdateRequiredResources() {
    boolean created = false;
    boolean updated = false;
    for (Tuple2<HasMetadata, Optional<HasMetadata>> requiredResource :
        context.getRequiredResources()) {
      Optional<HasMetadata> matchingResource = requiredResource.v2;
      if (matchingResource
          .map(existingResource -> handlerSelector.equals(
              context, existingResource, requiredResource.v1))
          .orElse(false)) {
        LOGGER.trace("Found resource {}.{} of type {}",
            requiredResource.v1.getMetadata().getNamespace(),
            requiredResource.v1.getMetadata().getName(),
            requiredResource.v1.getKind());
        continue;
      }
      if (matchingResource.isPresent()) {
        HasMetadata existingResource = matchingResource.get();
        if (handlerSelector.skipUpdate(context, existingResource, requiredResource.v1)) {
          LOGGER.trace("Skip update for resource {}.{} of type {}",
              requiredResource.v1.getMetadata().getNamespace(),
              requiredResource.v1.getMetadata().getName(),
              requiredResource.v1.getKind());
          continue;
        }
        LOGGER.debug("Updating resource {}.{} of type {}"
            + " to meet " + name + " requirements",
            existingResource.getMetadata().getNamespace(),
            existingResource.getMetadata().getName(),
            existingResource.getKind());
        HasMetadata updatedExistingResource = Unchecked.supplier(() -> objectMapper.treeToValue(
            objectMapper.valueToTree(existingResource), existingResource.getClass())).get();
        handlerSelector.update(context, updatedExistingResource, requiredResource.v1);
        HasMetadata patchedResource = handlerSelector
            .patch(client, context, updatedExistingResource);
        if (!handlerSelector.equals(context, existingResource, patchedResource)) {
          updated = true;
        }
      } else {
        if (handlerSelector.skipCreation(context, requiredResource.v1)) {
          LOGGER.trace("Skip creation for resource {}.{} of type {}",
              requiredResource.v1.getMetadata().getNamespace(),
              requiredResource.v1.getMetadata().getName(),
              requiredResource.v1.getKind());
          continue;
        }
        LOGGER.debug("Creating resource {}.{} of type {}",
            requiredResource.v1.getMetadata().getNamespace(),
            requiredResource.v1.getMetadata().getName(),
            requiredResource.v1.getKind());
        HasMetadata updatedRequiredResource = Unchecked.supplier(() -> objectMapper.treeToValue(
            objectMapper.valueToTree(requiredResource.v1), requiredResource.v1.getClass())).get();
        handlerSelector.update(context, updatedRequiredResource, requiredResource.v1);
        HasMetadata createdResource = handlerSelector
            .create(client, context, updatedRequiredResource);
        if (createdResource != null) {
          created = true;
        }
      }
    }

    if (updated) {
      return Operation.UPDATED;
    }

    if (created && ! updated) {
      return Operation.CREATED;
    }

    return Operation.NONE;
  }

  protected void onPreConfigReconcilied() {
  }

  protected void onConfigCreated() {
  }

  protected void onConfigUpdated() {
  }

  protected void onPostConfigReconcilied() {
  }

}
