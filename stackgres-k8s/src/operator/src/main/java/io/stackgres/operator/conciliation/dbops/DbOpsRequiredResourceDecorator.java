/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.dbops;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.stackgres.common.CdiUtil;
import io.stackgres.operator.conciliation.AbstractRequiredResourceDecorator;
import io.stackgres.operator.conciliation.ResourceGenerationDiscoverer;
import io.stackgres.operator.conciliation.factory.DecoratorDiscoverer;

@ApplicationScoped
public class DbOpsRequiredResourceDecorator
    extends AbstractRequiredResourceDecorator<StackGresDbOpsContext> {

  @Inject
  public DbOpsRequiredResourceDecorator(
      DecoratorDiscoverer<StackGresDbOpsContext> decoratorDiscoverer,
      ResourceGenerationDiscoverer<StackGresDbOpsContext> generators) {
    super(decoratorDiscoverer, generators);
  }

  public DbOpsRequiredResourceDecorator() {
    super(null, null);
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
  }

}
