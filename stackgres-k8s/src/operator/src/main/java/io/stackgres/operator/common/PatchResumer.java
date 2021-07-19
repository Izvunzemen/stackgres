/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.common;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.conciliation.ComparisonDelegator;
import io.stackgres.operator.conciliation.ReconciliationResult;
import org.apache.commons.compress.utils.Lists;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

public class PatchResumer {

  static final List<String> ORDERED_RESOURCE_KINDS = ImmutableList.of(
      StackGresCluster.KIND,
      HasMetadata.getKind(StatefulSet.class),
      HasMetadata.getKind(Pod.class),
      HasMetadata.getKind(Service.class),
      HasMetadata.getKind(Endpoints.class),
      HasMetadata.getKind(ServiceAccount.class),
      HasMetadata.getKind(Role.class),
      HasMetadata.getKind(RoleBinding.class),
      HasMetadata.getKind(CronJob.class),
      HasMetadata.getKind(Job.class),
      HasMetadata.getKind(PersistentVolumeClaim.class),
      HasMetadata.getKind(ConfigMap.class),
      HasMetadata.getKind(Secret.class)
      );

  static final long MAX_RESUME_LENGTH = 256;

  private final ComparisonDelegator<?> resourceComparator;

  public PatchResumer(ComparisonDelegator<?> resourceComparator) {
    super();
    this.resourceComparator = resourceComparator;
  }

  public String resourceChanged(StackGresCluster cluster, ReconciliationResult result) {
    var created = Seq.seq(result.getCreations())
        .sorted(this::compareResourceByKind)
        .map(r -> Tuple.tuple("+" + r.getKind() + ":" + r.getMetadata().getName(), "created"));
    var deleted = Seq.seq(result.getDeletions())
        .sorted(this::compareResourceByKind)
        .map(r -> Tuple.tuple("-" + r.getKind() + ":" + r.getMetadata().getName(), "deleted"));
    var patched = Seq.seq(result.getPatches())
        .flatMap(t -> Seq.seq(resourceComparator.getJsonDiff(t.v1, t.v2))
            .map(patch -> Tuple.tuple(t.v2, patch)))
        .map(t -> resumeResourcePatch(t.v1, t.v2, cluster))
        .sorted((t1, t2) -> compareResourceByKind(t1.v1, t2.v1))
        .map(t -> Tuple.tuple(
            t.v1.getKind() + ":" + t.v1.getMetadata().getName()
            + " (" + t.v2 + ")", "patched"));
    var changes = created
        .append(deleted)
        .append(patched)
        .map(change -> change.concat(change.v1.length()))
        .reduce(Lists.<Tuple3<String, String, Long>>newArrayList(),
            (list, change) -> {
              list.add(change.map3(length -> length
                    + Seq.seq(list).findLast().map(Tuple3::v3).orElse(0L)
                    + ", ".length()));
              return list;
            },
            (u, v) -> u);
    long nextMaxLength = MAX_RESUME_LENGTH;
    while (true) {
      final long maxLength = nextMaxLength;
      nextMaxLength = Seq.seq(changes)
          .limitUntil(t -> t.v3 > maxLength)
          .map(Tuple3::v3)
          .findLast()
          .map(l -> l - 1)
          .orElse(0L);
      String longSummary = Seq.seq(changes)
          .limitUntil(t -> t.v3 > maxLength)
          .map(Tuple3::v1)
          .toString(", ");
      String shortSummary = Seq.seq(changes)
          .skipUntil(t -> t.v3 > maxLength)
          .grouped(Tuple3::v2)
          .map(t -> t.v2.count() + " resources where " + t.v1)
          .toString(", ");
      final String summary;
      if (shortSummary.isEmpty()) {
        summary = longSummary;
      } else {
        summary = longSummary + " and other " + shortSummary;
      }
      if (summary.length() <= MAX_RESUME_LENGTH) {
        return summary;
      }
      if (nextMaxLength <= 0L) {
        return shortSummary;
      }
    }
  }

  private int compareResourceByKind(HasMetadata leftResource, HasMetadata rightResource) {
    Optional<Integer> leftResourceOrder =
        Optional.of(ORDERED_RESOURCE_KINDS.indexOf(leftResource.getKind()))
        .filter(i -> i >= 0);
    Optional<Integer> rightResourceOrder =
        Optional.of(ORDERED_RESOURCE_KINDS.indexOf(rightResource.getKind()))
        .filter(i -> i >= 0);
    if (leftResourceOrder.isPresent() && rightResourceOrder.isPresent()) {
      return leftResourceOrder.get() - rightResourceOrder.get();
    }
    if (leftResourceOrder.isPresent()) {
      return -1;
    }
    if (rightResourceOrder.isPresent()) {
      return 1;
    }
    return leftResource.getKind().compareTo(rightResource.getKind());
  }

  private Tuple2<HasMetadata, String> resumeResourcePatch(HasMetadata resource,
      JsonNode patch, StackGresCluster cluster) {
    if (resource.getKind().equals(HasMetadata.getKind(StatefulSet.class))
        && patch.get("op").asText().equals("replace")
        && patch.get("path").asText().equals("/spec/replicas")) {
      int replicas = ((StatefulSet) resource).getSpec().getReplicas();
      if (cluster.getSpec().getInstances() != replicas) {
        return Tuple.tuple(resource, "Instances have been updated to "
            + ((StatefulSet) resource).getSpec().getReplicas()
            + " (cluster instances are now " + cluster.getSpec().getInstances() + ")");
      } else {
        return Tuple.tuple(resource, "Instances have been updated to "
          + replicas);
      }
    }
    return Tuple.tuple(resource, resumeFieldPatch(patch,
        resource.getKind().equals(HasMetadata.getKind(Secret.class))));
  }

  private String resumeFieldPatch(JsonNode patch, boolean secret) {
    String op = patch.get("op").asText();
    if (op.equals("add") || op.equals("replace")) {
      final String prefix;
      if (op.equals("replace")) {
        prefix = "";
      } else {
        prefix = "+";
      }
      final JsonNode value = patch.get("value");
      final String valueAsText;
      if (secret && patch.get("path").asText().startsWith("/data")) {
        valueAsText = "***";
      } else if (value.isTextual()) {
        valueAsText = value.asText();
      } else {
        valueAsText = value.toString();
      }
      return prefix + patch.get("path").asText() + " -> " + valueAsText;
    }
    if (op.equals("remove")) {
      return "-" + patch.get("path").asText();
    }
    return "?" + patch.toString();
  }

}
