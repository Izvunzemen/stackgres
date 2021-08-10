/*
 * Copyright (C) 2021 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.jobs.dbops;

import static io.stackgres.jobs.dbops.clusterrestart.ClusterRestartImpl.REDUCED_IMPACT_METHOD;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.smallrye.mutiny.Uni;
import io.stackgres.common.ClusterPendingRestartUtil;
import io.stackgres.common.ClusterPendingRestartUtil.RestartReasons;
import io.stackgres.common.LabelFactory;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.crd.sgcluster.ClusterDbOpsRestartStatus;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterStatus;
import io.stackgres.common.crd.sgdbops.DbOpsRestartStatus;
import io.stackgres.common.crd.sgdbops.StackGresDbOps;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsRestart;
import io.stackgres.common.crd.sgdbops.StackGresDbOpsSpec;
import io.stackgres.common.resource.CustomResourceFinder;
import io.stackgres.common.resource.CustomResourceScheduler;
import io.stackgres.common.resource.ResourceFinder;
import io.stackgres.common.resource.ResourceScanner;
import io.stackgres.jobs.dbops.clusterrestart.ClusterRestart;
import io.stackgres.jobs.dbops.clusterrestart.ClusterRestartState;
import io.stackgres.jobs.dbops.clusterrestart.ClusterRestartStateHandlerImpl;
import io.stackgres.jobs.dbops.clusterrestart.ImmutableClusterRestartState;
import io.stackgres.jobs.dbops.clusterrestart.InvalidCluster;
import io.stackgres.jobs.dbops.clusterrestart.RestartEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestartStateHandler implements ClusterRestartStateHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ClusterRestartStateHandlerImpl.class);

  @Inject
  ClusterRestart clusterRestart;

  @Inject
  CustomResourceFinder<StackGresDbOps> dbOpsFinder;

  @Inject
  CustomResourceFinder<StackGresCluster> clusterFinder;

  @Inject
  LabelFactory<StackGresCluster> labelFactory;

  @Inject
  ResourceFinder<StatefulSet> statefulSetFinder;

  @Inject
  ResourceScanner<Pod> podScanner;

  @Inject
  CustomResourceScheduler<StackGresDbOps> dbOpsScheduler;

  @Inject
  CustomResourceScheduler<StackGresCluster> clusterScheduler;

  @Override
  public Uni<ClusterRestartState> restartCluster(StackGresDbOps dbOps) {
    String clusterName = dbOps.getSpec().getSgCluster();
    String dbOpsName = dbOps.getMetadata().getName();
    String namespace = dbOps.getMetadata().getNamespace();

    return getClusterRestartState(namespace, dbOpsName, clusterName)
        .call(this::initClusterDbOpsStatus)
        .call(clusterRestartState -> initDbOpsStatus(clusterRestartState, dbOps))
        .onFailure()
        .retry()
        .withBackOff(Duration.ofMillis(10), Duration.ofSeconds(5))
        .atMost(10)
        .chain(this::restartCluster);
  }

  private Uni<ClusterRestartState> restartCluster(ClusterRestartState clusterRestartState) {
    return clusterRestart.restartCluster(clusterRestartState)
        .onItem()
        .call(event -> updateJobStatus(event, clusterRestartState))
        .onItem()
        .call(event -> logEvent(clusterRestartState.getClusterName(), event))
        .onFailure()
        .call(error -> reportFailure(clusterRestartState.getClusterName(), error))
        .collect()
        .last()
        .call(() -> findSgCluster(clusterRestartState.getClusterName(),
            clusterRestartState.getNamespace())
            .chain(this::cleanCluster)
            .onFailure()
            .retry()
            .withBackOff(Duration.ofMillis(10), Duration.ofSeconds(5))
            .indefinitely())
        .chain(ignore -> Uni.createFrom().item(clusterRestartState));
  }

  protected abstract void cleanClusterStatus(StackGresCluster cluster);

  protected Uni<StackGresDbOps> updateJobStatus(RestartEvent event,
      ClusterRestartState clusterRestartState) {
    return findDbOps(clusterRestartState.getDbOpsName(), clusterRestartState.getNamespace())
        .chain(dbOps -> updateJobStatus(dbOps, event))
        .onFailure()
        .retry()
        .withBackOff(Duration.ofMillis(10), Duration.ofSeconds(5))
        .atMost(10);
  }

  private Uni<StackGresDbOps> updateJobStatus(StackGresDbOps dbOps, RestartEvent event) {
    return Uni.createFrom().emitter(em -> {
      var restartStatus = getDbOpRestartStatus(dbOps);

      var podName = event.getPod().getMetadata().getName();

      switch (event.getEventType()) {
        case SWITCHOVER_INITIATED:
          restartStatus.setSwitchoverInitiated(Instant.now().toString());
          break;
        case SWITCHOVER_FINALIZED:
          restartStatus.setSwitchoverFinalized(Instant.now().toString());
          break;
        case POD_RESTART:
          List<String> pendingInstances = restartStatus.getPendingToRestartInstances();
          pendingInstances.remove(podName);
          if (restartStatus.getRestartedInstances() == null) {
            restartStatus.setRestartedInstances(new ArrayList<>());
          }
          restartStatus.getRestartedInstances().add(podName);
          break;
        case POD_CREATED:
          if (restartStatus.getRestartedInstances() == null) {
            restartStatus.setRestartedInstances(new ArrayList<>());
          }
          restartStatus.getRestartedInstances().add(podName);
          break;
        default:
      }

      setDbOpRestartStatus(dbOps, restartStatus);
      var newStatus = dbOpsScheduler.update(dbOps);
      em.complete(newStatus);
    });
  }

  protected abstract boolean isSgClusterDbOpsStatusInitialized(StackGresCluster cluster);

  protected abstract boolean isDbOpsStatusInitialized(StackGresDbOps cluster);

  protected Uni<List<Pod>> scanClusterPods(StackGresCluster cluster) {
    return Uni.createFrom().emitter(em -> {

      String namespace = cluster.getMetadata().getNamespace();

      final Map<String, String> podLabels = labelFactory.patroniClusterLabels(cluster);

      List<Pod> clusterPods = podScanner.findByLabelsAndNamespace(namespace, podLabels);

      em.complete(clusterPods);
    });
  }

  protected Uni<ClusterRestartState> getClusterRestartState(
      String namespace, String dbOpsName, String clusterName) {
    return Uni.combine().all().unis(
        findDbOps(dbOpsName, namespace),
        findSgCluster(clusterName, namespace)
        .chain(cluster -> Uni.combine().all().unis(
            Uni.createFrom().item(cluster),
            getClusterStatefulSet(cluster),
            scanClusterPods(cluster))
            .asTuple()))
        .asTuple()
        .onItem()
        .transform(tuple -> buildClusterRestartState(
            tuple.getItem1(), tuple.getItem2().getItem1(),
            tuple.getItem2().getItem2(), tuple.getItem2().getItem3()));
  }

  protected Uni<?> initClusterDbOpsStatus(ClusterRestartState clusterRestartState) {
    return findSgCluster(clusterRestartState.getClusterName(), clusterRestartState.getNamespace())
        .chain(cluster -> {
          if (isSgClusterDbOpsStatusInitialized(cluster)) {
            return Uni.createFrom().voidItem();
          } else {
            return initRestartStatusValues(clusterRestartState, cluster)
                .onItem()
                .invoke(v -> clusterScheduler.updateStatus(cluster));
          }
        });
  }

  protected Uni<?> initDbOpsStatus(ClusterRestartState clusterRestartState, StackGresDbOps dbOps) {
    if (isDbOpsStatusInitialized(dbOps)) {
      return Uni.createFrom().voidItem();
    } else {
      return initDbOpsRestartStatusValues(clusterRestartState, dbOps)
          .onItem()
          .transform(v -> dbOpsScheduler.update(dbOps));
    }
  }

  protected Uni<Void> initDbOpsRestartStatusValues(ClusterRestartState clusterRestartState,
      StackGresDbOps dbOps) {
    var restartStatus = getDbOpRestartStatus(dbOps);

    restartStatus.setInitialInstances(
        clusterRestartState.getInitialInstances()
        .stream()
        .map(Pod::getMetadata)
        .map(ObjectMeta::getName)
        .sorted(String::compareTo)
        .collect(Collectors.toList()));
    restartStatus.setPendingToRestartInstances(
        clusterRestartState.getInitialInstances()
        .stream()
        .filter(clusterRestartState::hasToBeRestarted)
        .map(Pod::getMetadata)
        .map(ObjectMeta::getName)
        .sorted(String::compareTo)
        .collect(Collectors.toList()));
    restartStatus.setPrimaryInstance(
        clusterRestartState.getPrimaryInstance()
        .getMetadata().getName());
    return Uni.createFrom().voidItem();
  }

  private @NotNull Uni<Optional<StatefulSet>> getClusterStatefulSet(StackGresCluster cluster) {
    return Uni.createFrom().item(() -> statefulSetFinder.findByNameAndNamespace(
        cluster.getMetadata().getName(), cluster.getMetadata().getNamespace()));
  }

  protected abstract Optional<String> getRestartMethod(StackGresDbOps op);

  private Uni<Void> logEvent(String clusterName, RestartEvent event) {
    switch (event.getEventType()) {
      case POD_CREATED:
        LOGGER.info("Pod {} created", event.getPod().getMetadata().getName());
        break;
      case SWITCHOVER_INITIATED:
        LOGGER.info("Switchover of cluster {} performed", clusterName);
        break;
      case POSTGRES_RESTART:
        LOGGER.info("Postgres of pod {} restarted", event.getPod().getMetadata().getName());
        break;
      default:
        LOGGER.info("Pod {} restarted", event.getPod().getMetadata().getName());
    }
    return Uni.createFrom().voidItem();
  }

  protected Uni<Void> reportFailure(String clusterName, Throwable error) {
    LOGGER.error("Unexpected error on restarting cluster {}", clusterName, error);
    return Uni.createFrom().voidItem();
  }

  protected abstract DbOpsRestartStatus getDbOpRestartStatus(StackGresDbOps dbOps);

  protected abstract void setDbOpRestartStatus(StackGresDbOps dbOps,
      DbOpsRestartStatus dbOpsStatus);

  protected abstract ClusterDbOpsRestartStatus getClusterRestartStatus(StackGresCluster dbOps);

  protected ClusterRestartState buildClusterRestartState(StackGresDbOps dbOps,
      StackGresCluster cluster, Optional<StatefulSet> statefulSet, List<Pod> clusterPods) {
    DbOpsRestartStatus restartStatus = getDbOpRestartStatus(dbOps);
    Map<String, Pod> podsDict = clusterPods.stream()
        .collect(Collectors.toMap(pod -> pod.getMetadata().getName(), Function.identity()));

    var initialInstances = Optional.ofNullable(restartStatus.getInitialInstances())
        .map(instances -> instances.stream().map(podsDict::get)
            .collect(Collectors.toUnmodifiableList()))
        .orElse(clusterPods);

    var restartedInstances = Optional.ofNullable(restartStatus.getRestartedInstances())
        .map(instances -> instances.stream().map(podsDict::get)
            .collect(Collectors.toUnmodifiableList()))
        .orElse(List.of());

    var podRestartReasonsMap = clusterPods.stream()
        .collect(Collectors.toUnmodifiableMap(
            Function.identity(),
            pod -> getPodRestartReasons(cluster, statefulSet, pod)));

    final String method = getRestartMethod(dbOps)
        .orElse(REDUCED_IMPACT_METHOD);

    final boolean onlyPendingRestart = Optional.of(dbOps.getSpec())
        .map(StackGresDbOpsSpec::getRestart)
        .map(StackGresDbOpsRestart::getOnlyPendingRestart)
        .orElse(false);

    return ImmutableClusterRestartState.builder()
        .namespace(dbOps.getMetadata().getNamespace())
        .dbOpsName(dbOps.getMetadata().getName())
        .dbOpsOperation(dbOps.getSpec().getOp())
        .clusterName(cluster.getMetadata().getName())
        .restartMethod(method)
        .isOnlyPendingRestart(onlyPendingRestart)
        .primaryInstance(getPrimaryInstance(clusterPods))
        .isSwitchoverInitiated(restartStatus.getSwitchoverInitiated() != null)
        .isSwitchoverFinalized(restartStatus.getSwitchoverFinalized() != null)
        .initialInstances(initialInstances)
        .restartedInstances(restartedInstances)
        .totalInstances(clusterPods)
        .podRestartReasonsMap(podRestartReasonsMap)
        .build();
  }

  private RestartReasons getPodRestartReasons(StackGresCluster cluster,
      Optional<StatefulSet> statefulSet, Pod pod) {
    return ClusterPendingRestartUtil.getRestartReasons(
        Optional.ofNullable(cluster.getStatus())
        .map(StackGresClusterStatus::getPodStatuses)
        .orElse(ImmutableList.of()),
        statefulSet,
        ImmutableList.of(pod));
  }

  protected Pod getPrimaryInstance(List<Pod> pods) {
    return pods.stream()
        .filter(pod -> StackGresContext.PRIMARY_ROLE.equals(
            pod.getMetadata().getLabels().get(StackGresContext.ROLE_KEY)))
        .findFirst().orElseThrow(() -> new InvalidCluster("Cluster has no primary pod"));
  }

  protected Uni<Void> initRestartStatusValues(ClusterRestartState clusterRestartState,
      StackGresCluster cluster) {
    var restartStatus = getClusterRestartStatus(cluster);

    restartStatus.setInitialInstances(
        clusterRestartState.getInitialInstances()
        .stream()
        .map(Pod::getMetadata)
        .map(ObjectMeta::getName)
        .sorted(String::compareTo)
        .collect(Collectors.toList()));
    restartStatus.setPrimaryInstance(clusterRestartState.getPrimaryInstance()
        .getMetadata().getName());
    return Uni.createFrom().voidItem();
  }

  protected Uni<StackGresCluster> findSgCluster(String name, String namespace) {
    return Uni.createFrom().emitter(em ->
        clusterFinder.findByNameAndNamespace(name, namespace).ifPresentOrElse(
            em::complete,
            () -> em.fail(new IllegalArgumentException("SGCluster " + name + " not found"))
        ));
  }

  protected Uni<StackGresCluster> cleanCluster(StackGresCluster cluster) {
    return Uni.createFrom().emitter(em -> {
      cleanClusterStatus(cluster);
      var updatedCluster = clusterScheduler.updateStatus(cluster);
      em.complete(updatedCluster);
    });
  }

  protected Uni<StackGresDbOps> findDbOps(String name, String namespace) {
    return Uni.createFrom().emitter(em ->
        dbOpsFinder.findByNameAndNamespace(name, namespace).ifPresentOrElse(
            em::complete,
            () -> em.fail(new IllegalArgumentException("SGDbOps " + name + " not found"))
        )
    );
  }
}
