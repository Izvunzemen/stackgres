/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

import io.stackgres.common.crd.ConfigMapKeySelector;
import io.stackgres.common.crd.SecretKeySelector;
import io.stackgres.common.crd.Toleration;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterInitData;
import io.stackgres.common.crd.sgcluster.StackGresClusterPodScheduling;
import io.stackgres.common.crd.sgcluster.StackGresClusterPostgres;
import io.stackgres.common.crd.sgcluster.StackGresClusterScriptEntry;
import io.stackgres.common.crd.sgcluster.StackGresClusterScriptFrom;
import io.stackgres.common.crd.sgcluster.StackGresClusterSpec;
import io.stackgres.common.crd.sgcluster.StackGresClusterSsl;
import io.stackgres.common.crd.sgcluster.StackGresPodPersistentVolume;
import io.stackgres.operator.common.StackGresClusterReview;
import io.stackgres.operator.validation.ConstraintValidationTest;
import io.stackgres.operator.validation.ConstraintValidator;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    review.getRequest().getObject().getSpec().getPod().getPersistentVolume().setSize(null);

    checkNotNullErrorCause(StackGresPodPersistentVolume.class, "spec.pod.persistentVolume.size",
        review);
  }

  @Test
  void invalidVolumeSize_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod().getPersistentVolume().setSize("512");

    checkErrorCause(StackGresPodPersistentVolume.class, "spec.pod.persistentVolume.size",
        review, Pattern.class);
  }

  @Test
  void validScript_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScript("SELECT 1");

    validator.validate(review);
  }

  @Test
  void missingScript_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());

    checkErrorCause(StackGresClusterScriptEntry.class,
        new String[] {"spec.initData.scripts[0].script",
            "spec.initData.scripts[0].scriptFrom"},
        "isScriptMutuallyExclusiveAndRequired", review, AssertTrue.class);
  }

  @Test
  void validScriptAndScriptFrom_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScript("SELECT 1");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setKey("test");

    checkErrorCause(StackGresClusterScriptEntry.class,
        new String[] {"spec.initData.scripts[0].script",
            "spec.initData.scripts[0].scriptFrom"},
        "isScriptMutuallyExclusiveAndRequired", review, AssertTrue.class);
  }

  @Test
  void scriptWithEmptyDatabaseName_shouldFail() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts().add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setDatabase("");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).setScript("SELECT 1");

    checkErrorCause(StackGresClusterScriptEntry.class,
        new String[] { "spec.initData.scripts[0].database" },
        "isDatabaseNameNonEmpty", review, AssertTrue.class);
  }

  @Test
  void validScriptFromConfigMap_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setKey("test");

    validator.validate(review);
  }

  @Test
  void validScriptFromSecret_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setKey("test");

    validator.validate(review);
  }

  @Test
  void missingScriptFrom_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());

    checkErrorCause(StackGresClusterScriptFrom.class,
        new String[] {"spec.initData.scripts[0].scriptFrom.secretKeyRef",
            "spec.initData.scripts[0].scriptFrom.configMapKeyRef"},
        "isSecretKeySelectorAndConfigMapKeySelectorMutuallyExclusiveAndRequired",
        review, AssertTrue.class);
  }

  @Test
  void validScriptFromConfigMapAndSecret_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setKey("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setKey("test");

    checkErrorCause(StackGresClusterScriptFrom.class,
        new String[] {"spec.initData.scripts[0].scriptFrom.secretKeyRef",
            "spec.initData.scripts[0].scriptFrom.configMapKeyRef"},
        "isSecretKeySelectorAndConfigMapKeySelectorMutuallyExclusiveAndRequired",
        review, AssertTrue.class);
  }

  @Test
  void scriptFromConfigMapWithEmptyKey_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setKey("");

    checkErrorCause(SecretKeySelector.class,
        "spec.initData.scripts[0].scriptFrom.configMapKeyRef.key",
        "isKeyNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromConfigMapWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setConfigMapKeyRef(new ConfigMapKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setName("");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getConfigMapKeyRef().setKey("test");

    checkErrorCause(SecretKeySelector.class,
        "spec.initData.scripts[0].scriptFrom.configMapKeyRef.name",
        "isNameNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromSecretWithEmptyKey_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setName("test");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setKey("");

    checkErrorCause(SecretKeySelector.class, "spec.initData.scripts[0].scriptFrom.secretKeyRef.key",
        "isKeyNotEmpty", review, AssertTrue.class);
  }

  @Test
  void scriptFromSecretWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setInitData(new StackGresClusterInitData());
    review.getRequest().getObject().getSpec().getInitData().setScripts(new ArrayList<>());
    review.getRequest().getObject().getSpec().getInitData().getScripts()
        .add(new StackGresClusterScriptEntry());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0)
        .setScriptFrom(new StackGresClusterScriptFrom());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .setSecretKeyRef(new SecretKeySelector());
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setName("");
    review.getRequest().getObject().getSpec().getInitData().getScripts().get(0).getScriptFrom()
        .getSecretKeyRef().setKey("test");

    checkErrorCause(SecretKeySelector.class,
        "spec.initData.scripts[0].scriptFrom.secretKeyRef.name",
        "isNameNotEmpty", review, AssertTrue.class);
  }

  @Test
  void validNodeSelector_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setNodeSelector(new HashMap<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getNodeSelector().put("test",
        "true");

    validator.validate(review);
  }

  @Test
  void invalidNodeSelector_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setNodeSelector(new HashMap<>());

    checkErrorCause(StackGresClusterPodScheduling.class, "spec.pod.scheduling.nodeSelector",
        "isNodeSelectorNotEmpty", review, AssertTrue.class);
  }

  @Test
  void validToleration_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations()
        .add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setKey("test");

    validator.validate(review);
  }

  @Test
  void validTolerationKeyEmpty_shouldPass() throws ValidationFailed {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations()
        .add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setKey("");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setOperator("Exists");

    validator.validate(review);
  }

  @Test
  void invalidTolerationKeyEmpty_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations()
        .add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setKey("");

    checkErrorCause(Toleration.class,
        new String[] {"spec.pod.scheduling.tolerations[0].key",
            "spec.pod.scheduling.tolerations[0].operator"},
        "isOperatorExistsWhenKeyIsEmpty", review,
        AssertTrue.class);
  }

  @Test
  void invalidTolerationOperator_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations()
        .add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setKey("test");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setOperator("NotExists");

    checkErrorCause(Toleration.class, "spec.pod.scheduling.tolerations[0].operator",
        "isOperatorValid", review, AssertTrue.class);
  }

  @Test
  void invalidTolerationEffect_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().getPod()
        .setScheduling(new StackGresClusterPodScheduling());
    review.getRequest().getObject().getSpec().getPod().getScheduling()
        .setTolerations(new ArrayList<>());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations()
        .add(new Toleration());
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setKey("test");
    review.getRequest().getObject().getSpec().getPod().getScheduling().getTolerations().get(0)
        .setEffect("NeverSchedule");

    checkErrorCause(Toleration.class, "spec.pod.scheduling.tolerations[0].effect",
        "isEffectValid", review, AssertTrue.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"hardcover-lady-somebody-arrives-specialty-risk-stocking-nodes-"
      + "fisheries-introduces-5",
      "suzuki-stroke-rail-remix-suite-flux-diploma-slip-airfare-extremely-1",
  "mozilla-rose-types-border-biome-upright-weak-promote-monday-1234"})
  void invalidLongNames_shouldFail(String name) {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getMetadata().setName(name);

    ValidationFailed message =
        assertThrows(ValidationFailed.class, () -> validator.validate(review));
    assertEquals("Valid name must be 53 characters or less", message.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"stackgres.io/", "*9stackgres", "1143", "1143a", "-1143a", ".demo",
      "123-primary", "123-primary", "primary*", "stackgres-demo_1"})
  void invalidNames_shouldFail(String name) {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getMetadata().setName(name);

    ValidationFailed message =
        assertThrows(ValidationFailed.class, () -> validator.validate(review));
    assertEquals("Name must consist of lower case alphanumeric "
        + "characters or '-', start with an alphabetic character, "
        + "and end with an alphanumeric character", message.getMessage());
  }

  @Test
  void sslCertificateSecretNull_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setPostgres(new StackGresClusterPostgres());
    review.getRequest().getObject().getSpec().getPostgres().setSsl(new StackGresClusterSsl());
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setEnabled(true);
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setPrivateKeySecretKeySelector(
        new SecretKeySelector("test", "test"));

    checkErrorCause(StackGresClusterSsl.class,
        "spec.postgres.ssl.certificateSecretKeySelector",
        "isNotEnabledCertificateSecretKeySelectorRequired", review, AssertTrue.class);
  }

  @Test
  void sslPrivateKeySecretNull_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setPostgres(new StackGresClusterPostgres());
    review.getRequest().getObject().getSpec().getPostgres().setSsl(new StackGresClusterSsl());
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setEnabled(true);
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setCertificateSecretKeySelector(
        new SecretKeySelector("test", "test"));

    checkErrorCause(StackGresClusterSsl.class,
        "spec.postgres.ssl.privateKeySecretKeySelector",
        "isNotEnabledSecretKeySecretKeySelectorRequired", review, AssertTrue.class);
  }

  @Test
  void sslCertificateSecretWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setPostgres(new StackGresClusterPostgres());
    review.getRequest().getObject().getSpec().getPostgres().setSsl(new StackGresClusterSsl());
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setEnabled(true);
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setCertificateSecretKeySelector(
        new SecretKeySelector("test", null));
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setPrivateKeySecretKeySelector(
        new SecretKeySelector("test", "test"));

    checkErrorCause(SecretKeySelector.class,
        "spec.postgres.ssl.certificateSecretKeySelector.name",
        "isNameNotEmpty", review, AssertTrue.class);
  }

  @Test
  void sslPrivateKeySecretWithEmptyName_shouldFail() {
    StackGresClusterReview review = getValidReview();
    review.getRequest().getObject().getSpec().setPostgres(new StackGresClusterPostgres());
    review.getRequest().getObject().getSpec().getPostgres().setSsl(new StackGresClusterSsl());
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setEnabled(true);
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setCertificateSecretKeySelector(
        new SecretKeySelector("test", "test"));
    review.getRequest().getObject().getSpec().getPostgres().getSsl().setPrivateKeySecretKeySelector(
        new SecretKeySelector("test", null));

    checkErrorCause(SecretKeySelector.class,
        "spec.postgres.ssl.privateKeySecretKeySelector.name",
        "isNameNotEmpty", review, AssertTrue.class);
  }

}
