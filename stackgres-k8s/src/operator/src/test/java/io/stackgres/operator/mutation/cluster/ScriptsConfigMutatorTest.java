/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.mutation.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import io.stackgres.common.ManagedSqlUtil;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.StackGresVersion;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterManagedScriptEntry;
import io.stackgres.common.crd.sgcluster.StackGresClusterManagedScriptEntryScriptStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterManagedScriptEntryStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterManagedSqlStatus;
import io.stackgres.common.crd.sgcluster.StackGresClusterScriptEntry;
import io.stackgres.common.crd.sgscript.StackGresScript;
import io.stackgres.common.resource.CustomResourceFinder;
import io.stackgres.common.resource.CustomResourceScheduler;
import io.stackgres.operator.common.StackGresClusterReview;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptsConfigMutatorTest {

  protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();

  @Mock
  CustomResourceFinder<StackGresScript> scriptFinder;

  @Mock
  CustomResourceScheduler<StackGresScript> scriptScheduler;

  private ScriptsConfigMutator mutator;

  @BeforeEach
  void setUp() throws Exception {
    mutator = new ScriptsConfigMutator(scriptFinder, scriptScheduler);
  }

  @Test
  void createScriptAlreadyValid_shouldDoNothing() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation_with_managed_sql.json",
            StackGresClusterReview.class);

    JsonNode expectedCluster = JsonUtil.toJson(review.getRequest().getObject());

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertTrue(operations.isEmpty());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void createClusterWithDefaultScript_shouldDoNothing() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().setScripts(
        review.getRequest().getObject().getSpec().getManagedSql().getScripts().subList(0, 1));
    review.getRequest().getObject().getStatus().getManagedSql().setScripts(
        review.getRequest().getObject().getStatus().getManagedSql().getScripts().subList(0, 1));
    JsonNode expectedCluster = JsonUtil.toJson(review.getRequest().getObject());

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertTrue(operations.isEmpty());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void createClusterWithoutStatus_shouldAddStatusWithDefaultScript() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().setScripts(null);
    review.getRequest().getObject().getStatus().getManagedSql().setScripts(null);

    StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());
    expected.getSpec().getManagedSql().setScripts(new ArrayList<>());
    expected.getSpec().getManagedSql().getScripts().add(new StackGresClusterManagedScriptEntry());
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setId(0);
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setSgScript(ManagedSqlUtil.defaultName(expected));
    expected.getStatus().getManagedSql().setScripts(new ArrayList<>());
    expected.getStatus().getManagedSql().getScripts()
        .add(new StackGresClusterManagedScriptEntryStatus());
    expected.getStatus().getManagedSql().getScripts().get(0)
        .setId(0);
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    review.getRequest().getObject().setStatus(null);

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(2, operations.size());
    assertEquals(1, operations.stream().filter(o -> o instanceof ReplaceOperation).count());
    assertEquals(1, operations.stream().filter(o -> o instanceof AddOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void createClusterWithouIds_shouldAddThem() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation_with_managed_sql.json",
            StackGresClusterReview.class);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().get(0)
        .setScripts(null);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().get(1)
        .setScripts(null);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().get(2)
        .setScripts(null);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().get(3)
        .setScripts(null);
    final JsonNode expectedCluster = JsonUtil.toJson(review.getRequest().getObject());

    review.getRequest().getObject().getSpec().getManagedSql().getScripts().stream()
        .forEach(scriptEntry -> scriptEntry.setId(null));
    review.getRequest().getObject().getStatus().setManagedSql(
        new StackGresClusterManagedSqlStatus());

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(2, operations.size());
    assertEquals(2, operations.stream().filter(o -> o instanceof ReplaceOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterWithWithoutModification_shouldDoNothing() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    JsonNode expectedCluster = JsonUtil.toJson(review.getRequest().getObject());

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertTrue(operations.isEmpty());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterWithNoScripts_shouldAddDefaultScript() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().setScripts(
        null);
    StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());
    expected.getSpec().getManagedSql().setScripts(new ArrayList<>());
    expected.getSpec().getManagedSql().getScripts().add(new StackGresClusterManagedScriptEntry());
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setId(0);
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setSgScript(ManagedSqlUtil.defaultName(expected));
    expected.getStatus().getManagedSql().setScripts(new ArrayList<>());
    expected.getStatus().getManagedSql().getScripts()
        .add(new StackGresClusterManagedScriptEntryStatus());
    expected.getStatus().getManagedSql().getScripts().get(0)
        .setId(0);
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(2, operations.size());
    assertEquals(2, operations.stream().filter(o -> o instanceof ReplaceOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterWithoutStatus_shouldAddDefaultScriptAndStatus() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().setScripts(null);
    review.getRequest().getObject().getStatus().getManagedSql().setScripts(null);
    StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());
    expected.getSpec().getManagedSql().setScripts(new ArrayList<>());
    expected.getSpec().getManagedSql().getScripts().add(new StackGresClusterManagedScriptEntry());
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setId(0);
    expected.getSpec().getManagedSql().getScripts().get(0)
        .setSgScript(ManagedSqlUtil.defaultName(expected));
    expected.getStatus().getManagedSql().setScripts(new ArrayList<>());
    expected.getStatus().getManagedSql().getScripts()
        .add(new StackGresClusterManagedScriptEntryStatus());
    expected.getStatus().getManagedSql().getScripts().get(0)
        .setId(0);
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(2, operations.size());
    assertEquals(2, operations.stream().filter(o -> o instanceof ReplaceOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterAddingAnEntry_shouldSetIdAndVersion() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().getScripts()
        .add(1, new StackGresClusterManagedScriptEntry());

    StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());
    expected.getSpec().getManagedSql().getScripts().get(1).setId(4);
    expected.getStatus().getManagedSql().getScripts()
        .add(new StackGresClusterManagedScriptEntryStatus());
    expected.getStatus().getManagedSql().getScripts().get(4).setId(4);
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(2, operations.size());
    assertEquals(2, operations.stream().filter(o -> o instanceof ReplaceOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterFromInitialScripts_shouldRemoveInitialScriptsAndAddManagedSql()
      throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getMetadata().setAnnotations(new HashMap<>());
    review.getRequest().getObject().getMetadata().getAnnotations().put(
        StackGresContext.VERSION_KEY, StackGresVersion.V_1_2.getVersion());
    review.getRequest().getObject().getSpec().getManagedSql().getScripts().get(1).setSgScript(
        ManagedSqlUtil.initialDataName(review.getRequest().getObject()));
    review.getRequest().getObject().getSpec().getManagedSql().getScripts().remove(3);
    review.getRequest().getObject().getSpec().getManagedSql().getScripts().remove(2);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().remove(3);
    review.getRequest().getObject().getStatus().getManagedSql().getScripts().remove(2);

    final StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());

    review.getRequest().getObject().getSpec().setManagedSql(null);
    review.getRequest().getObject().getStatus().setManagedSql(null);
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScript("CREATE DATABASE test;");

    final List<JsonPatchOperation> operations = mutator.mutate(review);

    expected.getStatus().getManagedSql().getScripts().get(1).setScripts(new ArrayList<>());
    expected.getStatus().getManagedSql().getScripts().get(1).getScripts()
        .add(new StackGresClusterManagedScriptEntryScriptStatus());
    expected.getStatus().getManagedSql().getScripts().get(1).getScripts().get(0)
        .setId(0);
    expected.getStatus().getManagedSql().getScripts().get(1).getScripts().get(0)
        .setVersion(0);
    expected.getStatus().getManagedSql().getScripts().get(1).setStartedAt(
        review.getRequest().getObject()
        .getStatus().getManagedSql().getScripts().get(1).getStartedAt());
    expected.getStatus().getManagedSql().getScripts().get(1).setCompletedAt(
        review.getRequest().getObject()
        .getStatus().getManagedSql().getScripts().get(1).getCompletedAt());
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    assertEquals(3, operations.size());
    assertEquals(1, operations.stream().filter(o -> o instanceof RemoveOperation).count());
    assertEquals(2, operations.stream().filter(o -> o instanceof AddOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(1)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(1)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

  @Test
  void updateClusterRemovingAnEntry_shouldRemoveItsStatus() throws JsonPatchException {
    StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_update_with_managed_sql.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().getSpec().getManagedSql().getScripts().remove(1);
    StackGresCluster expected = JsonUtil.copy(review.getRequest().getObject());
    expected.getStatus().getManagedSql().getScripts().remove(1);
    JsonNode expectedCluster = JsonUtil.toJson(expected);

    List<JsonPatchOperation> operations = mutator.mutate(review);

    assertEquals(1, operations.size());
    assertEquals(1, operations.stream().filter(o -> o instanceof ReplaceOperation).count());

    JsonNode crJson = JsonUtil.toJson(review.getRequest().getObject());

    JsonPatch jp = new JsonPatch(operations);
    JsonNode actualCluster = jp.apply(crJson);

    JsonUtil.assertJsonEquals(expectedCluster, actualCluster);

    verify(scriptFinder, times(0)).findByNameAndNamespace(any(), any());
    verify(scriptScheduler, times(0)).create(any());
    verify(scriptScheduler, times(0)).update(any());
  }

}
