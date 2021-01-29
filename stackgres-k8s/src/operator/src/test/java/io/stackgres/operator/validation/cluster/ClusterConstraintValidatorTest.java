/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.cluster;

import java.util.ArrayList;
import java.util.HashMap;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

import io.stackgres.common.crd.ConfigMapKeySelector;
import io.stackgres.common.crd.SecretKeySelector;
import io.stackgres.common.crd.Toleration;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterInitData;
import io.stackgres.common.crd.sgcluster.StackGresClusterScriptEntry;
import io.stackgres.common.crd.sgcluster.StackGresClusterScriptFrom;
import io.stackgres.common.crd.sgcluster.StackGresClusterSpec;
import io.stackgres.common.crd.sgcluster.StackGresPodPersistentVolume;
import io.stackgres.common.crd.sgcluster.StackGresPodScheduling;
import io.stackgres.operator.common.StackGresClusterReview;
import io.stackgres.operator.validation.ConstraintValidationTest;
import io.stackgres.operator.validation.ConstraintValidator;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.Test;

class ClusterConstraintValidatorTest extends ConstraintValidationTest<StackGresClusterReview> {

  @Override
  protected ConstraintValidator<StackGresClusterReview> buildValidator() {
    return new ClusterConstraintValidator();
  }

  @Override
  protected StackGresClusterReview getValidReview() {
    return JsonUtil.readFromJson("cluster_allow_requests/valid_creation.json",
        StackGresClusterReview.class);
  }

  @Override
  protected StackGresClusterReview getInvalidReview() {
    final StackGresClusterReview review = JsonUtil
        .readFromJson("cluster_allow_requests/valid_creation.json",
            StackGresClusterReview.class);

    review.getRequest().getObject().setSpec(null);
    return review;
  }

  @Test
  void nullSpec_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().setSpec(null);

    checkNotNullErrorCause(StackGresCluster.class, "spec", review);
  }

  @Test
  void nullResourceProfile_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setResourceProfile(null);

    checkNotNullErrorCause(StackGresClusterSpec.class, "spec.resourceProfile", review);
  }

  @Test
  void nullVolumeSize_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().getPersistentVolume().setVolumeSize(null);

    checkNotNullErrorCause(StackGresPodPersistentVolume.class, "spec.pod.persistentVolume.volumeSize", review);
  }

  @Test
  void invalidVolumeSize_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().getPersistentVolume().setVolumeSize("512");

    checkErrorCause(StackGresPodPersistentVolume.class, "spec.pod.persistentVolume.volumeSize",
        review, Pattern.class);
  }

  @Test
  void validScript_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScript("SELECT 1");

    validator.validate(review);
  }

  @Test
  void missingScript_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());

    checkErrorCause(StackGresClusterScriptEntry.class,
        new String[] { "spec.initData.scripts[0].script",
            "spec.initData.scripts[0].scriptFrom" },
        "spec.pod.scripts[0].isScriptMutuallyExclusiveAndRequired", review, AssertTrue.class);
  }

  @Test
  void validScriptAndScriptFrom_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScript("SELECT 1");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setKey("test");

    checkErrorCause(StackGresClusterScriptEntry.class,
        new String[] { "spec.initData.scripts[0].script",
            "spec.initData.scripts[0].scriptFrom" },
        "spec.pod.scripts[0].isScriptMutuallyExclusiveAndRequired", review, AssertTrue.class);
  }

  @Test
  void validScriptFromConfigMap_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setKey("test");

    validator.validate(review);
  }

  @Test
  void validScriptFromSecret_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setKey("test");

    validator.validate(review);
  }

  @Test
  void missingScriptFrom_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());

    checkErrorCause(StackGresClusterScriptFrom.class,
        new String[] { "spec.initData.scripts[0].scriptFrom.secretKeyRef",
            "spec.initData.scripts[0].scriptFrom.configMapKeyRef" },
        "spec.pod.scripts[0].scriptFrom.isSecretKeySelectorAndConfigMapKeySelectorMutuallyExclusiveAndRequired",
        review, AssertTrue.class);
  }

  @Test
  void validScriptFromConfigMapAndSecret_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setKey("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setKey("test");

    checkErrorCause(StackGresClusterScriptFrom.class,
        new String[] { "spec.initData.scripts[0].scriptFrom.secretKeyRef",
            "spec.initData.scripts[0].scriptFrom.configMapKeyRef" },
        "spec.pod.scripts[0].scriptFrom.isSecretKeySelectorAndConfigMapKeySelectorMutuallyExclusiveAndRequired",
        review, AssertTrue.class);
  }

  @Test
  void scriptFromConfigMapWithEmptyKey_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setKey("");

    checkErrorCause(SecretKeySelector.class, "spec.initData.scripts[0].scriptFrom.configMapKeyRef.key",
        "spec.pod.scripts[0].scriptFrom.configMapKeyRef.isKeyNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromConfigMapWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setName("");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getConfigMapKeyRef().setKey("test");

    checkErrorCause(SecretKeySelector.class, "spec.initData.scripts[0].scriptFrom.configMapKeyRef.name",
        "spec.pod.scripts[0].scriptFrom.configMapKeyRef.isNameNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromSecretWithEmptyKey_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setKey("");

    checkErrorCause(SecretKeySelector.class, "spec.initData.scripts[0].scriptFrom.secretKeyRef.key",
        "spec.pod.scripts[0].scriptFrom.secretKeyRef.isKeyNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromSecretWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setName("");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom().getSecretKeyRef().setKey("test");

    checkErrorCause(SecretKeySelector.class, "spec.initData.scripts[0].scriptFrom.secretKeyRef.name",
        "spec.pod.scripts[0].scriptFrom.secretKeyRef.isNameNotEmpty", review, AssertTrue.class);
  }

  @Test
  void validNodeSelector_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setNodeSelector(new HashMap<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getNodeSelector().put("test", "true");

    validator.validate(review);
  }

  @Test
  void invalidNodeSelector_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setNodeSelector(new HashMap<>());

    checkErrorCause(StackGresPodScheduling.class, "spec.pod.scheduling.nodeSelector",
        "spec.pod.scheduling.isNodeSelectorNotEmpty", review, AssertTrue.class);
  }

  @Test
  void validToleration_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setKey("test");

    validator.validate(review);
  }

  @Test
  void validTolerationKeyEmpty_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setKey("");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setOperator("Exists");

    validator.validate(review);
  }

  @Test
  void invalidTolerationKeyEmpty_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setKey("");

    checkErrorCause(Toleration.class,
        new String[] { "spec.pod.scheduling.tolerations[0].key", "spec.pod.scheduling.tolerations[0].operator" },
        "spec.pod.scheduling.tolerations[0].isOperatorExistsWhenKeyIsEmpty", review, AssertTrue.class);
  }

  @Test
  void invalidTolerationOperator_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setKey("test");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setOperator("NotExists");

    checkErrorCause(Toleration.class, "spec.pod.scheduling.tolerations[0].operator",
        "spec.pod.scheduling.isOperatorValid", review, AssertTrue.class);
  }

  @Test
  void invalidTolerationEffect_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().setScheduling(new StackGresPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling().setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setKey("test");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0).setEffect("NeverSchedule");

    checkErrorCause(Toleration.class, "spec.pod.scheduling.tolerations[0].effect",
        "spec.pod.scheduling.isEffectValid", review, AssertTrue.class);
  }

}