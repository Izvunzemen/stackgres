/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops.clusterrestart;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Pod;
import io.smallrye.mutiny.Uni;
import io.stackgres.common.resource.ResourceWriter;

@ApplicationScoped
public class PodRestartImpl implements PodRestart {

  private final ResourceWriter<Pod> podWriter;

  private final Watcher<Pod> podWatcher;

  @Inject
  public PodRestartImpl(ResourceWriter<Pod> podWriter, Watcher<Pod> podWatcher) {
    this.podWriter = podWriter;
    this.podWatcher = podWatcher;
  }

  @Override
  public Uni<Pod> restartPod(Pod pod) {

    String podName = pod.getMetadata().getName();
    String podNamespace = pod.getMetadata().getNamespace();

    return deletePod(pod)
        .chain(() -> podWatcher.waitUntilIsReplaced(pod))
        .chain(() -> podWatcher.waitUntilIsReady(podName, podNamespace));
  }

  public Uni<Void> deletePod(Pod pod){
    return Uni.createFrom().emitter(em -> {
      podWriter.delete(pod);
      em.complete(null);
    });
  }
}
