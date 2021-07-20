/*
 * Copyright (C) 2020 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.quarkus.security.AuthenticationFailedException;
import io.stackgres.apiweb.security.SecretVerification;
import io.stackgres.apiweb.security.TokenResponse;
import io.stackgres.apiweb.security.TokenUtils;
import io.stackgres.apiweb.security.UserPassword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("auth")
@RequestScoped
public class LocalLoginResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalLoginResource.class);

  @Inject
  SecretVerification verify;

  @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "28800")
  long duration;

  @Operation(
      responses = {
          @ApiResponse(responseCode = "200", description = "OK",
              content = {@Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = TokenResponse.class))})
      })
  @CommonApiResponses
  @POST
  @Path("login")
  public Response login(@Valid UserPassword credentials) {
    try {
      String k8sUsername =
          verify.verifyCredentials(credentials.getUserName(), credentials.getPassword());
      LOGGER.info("Kubernetes user: {}", k8sUsername);
      String accessToken = TokenUtils.generateTokenString(k8sUsername, credentials.getUserName());

      TokenResponse tokenResponse = new TokenResponse();
      tokenResponse.setAccessToken(accessToken);
      tokenResponse.setTokenType("Bearer");
      tokenResponse.setExpiresIn(duration);

      return Response.ok(tokenResponse)
          .cacheControl(noCache())
          .build();
    } catch (AuthenticationFailedException e) {
      return Response.status(Status.FORBIDDEN)
          .cacheControl(noCache())
          .build();
    }
  }

  private CacheControl noCache() {
    CacheControl cc = new CacheControl();
    cc.setPrivate(true);
    cc.setNoCache(true);
    cc.setNoStore(true);
    return cc;
  }

}
