/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.base.Throwables;
import io.quarkus.security.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractGenericExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      AbstractGenericExceptionMapper.class);

  @Override
  public Response toResponse(T throwable) {
    int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
    Throwable cause = Throwables.getRootCause(throwable);
    if (cause instanceof WebApplicationException) {
      statusCode = ((WebApplicationException) cause).getResponse().getStatus();
    }

    if (cause instanceof UnauthorizedException) {
      return new UnauthorizedExceptionMapper().toResponse((UnauthorizedException) cause);
    }

    if (statusCode == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
      LOGGER.error("An error occurred in the REST API", throwable);
    }

    String message = cause.getMessage();

    return Response.status(statusCode).type(MediaType.APPLICATION_JSON)
        .entity(ErrorResponse.create(cause, message)).build();

  }
}
